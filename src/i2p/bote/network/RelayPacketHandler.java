/**
 * Copyright (C) 2009  HungryHobo@mail.i2p
 * 
 * The GPG fingerprint for HungryHobo@mail.i2p is:
 * 6DD3 EAA2 9990 29BC 4AD2 7486 1E2C 7B61 76DC DC12
 * 
 * This file is part of I2P-Bote.
 * I2P-Bote is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * I2P-Bote is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with I2P-Bote.  If not, see <http://www.gnu.org/licenses/>.
 */

package i2p.bote.network;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import i2p.bote.Util;
import i2p.bote.folder.RelayPacketFolder;
import i2p.bote.packet.CommunicationPacket;
import i2p.bote.packet.DataPacket;
import i2p.bote.packet.MalformedDataPacketException;
import i2p.bote.packet.RelayDataPacket;
import i2p.bote.packet.RelayRequest;
import i2p.bote.packet.dht.DhtStorablePacket;
import net.i2p.client.I2PSession;
import net.i2p.data.DataFormatException;
import net.i2p.data.Destination;
import net.i2p.util.Log;

/**
 * Receives {@link RelayDataPacket}s from other peers and forwards them
 * (for {@link RelayDataPacket} payloads) or stores them in the DHT
 * (for {@link DhtStorablePacket} payloads).
 */
public class RelayPacketHandler implements PacketListener {
    private static final int MAX_CONCURRENT_DHT_TASKS = 5;
    private static final int THREAD_STACK_SIZE = 262144;
    
    private Log log = new Log(RelayPacketHandler.class);
    private RelayPacketFolder relayPacketFolder;
    private DHT dht;
    private I2PSession i2pSession;
    private ExecutorService dhtTaskExecutor;

    public RelayPacketHandler(RelayPacketFolder relayPacketFolder, DHT dht, I2PSession i2pSession) {
        this.relayPacketFolder = relayPacketFolder;
        this.dht = dht;
        this.i2pSession = i2pSession;
        dhtTaskExecutor = Executors.newFixedThreadPool(MAX_CONCURRENT_DHT_TASKS, Util.createThreadFactory("DHTStoreTask", THREAD_STACK_SIZE));
    }
    
    @Override
    public void packetReceived(CommunicationPacket packet, Destination sender, long receiveTime) {
        if (packet instanceof RelayRequest && dht.isReady()) {
            RelayRequest relayRequest = (RelayRequest)packet;
            DataPacket dataPacket;
            try {
                dataPacket = relayRequest.getStoredPacket(i2pSession);
            }
            catch (DataFormatException e) {
                log.error("Invalid RelayDataPacket received from peer " + sender.toBase64(), e);
                return;
            }
            catch (MalformedDataPacketException e) {
                log.error("Invalid RelayDataPacket received from peer " + sender.toBase64(), e);
                return;
            }
            if (dataPacket instanceof RelayDataPacket) {
                RelayDataPacket relayDataPacket = (RelayDataPacket)dataPacket;
                relayPacketFolder.add(relayDataPacket);
            }
            else if (dataPacket instanceof DhtStorablePacket) {
                final DhtStorablePacket dhtPacket = (DhtStorablePacket)dataPacket;
                // do dht.store() in a separate thread so we don't block the notifier thread
                dhtTaskExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            dht.store(dhtPacket);
                            log.debug("Finished storing DHT packet: " + dhtPacket);
                        } catch (DhtException e) {
                            log.error("Error storing packet in the DHT: " + dhtPacket, e);
                        }
                    }
                });
            }
            else
                log.error("Don't know how to handle relay packet of type " + dataPacket.getClass());
        }
    }
}