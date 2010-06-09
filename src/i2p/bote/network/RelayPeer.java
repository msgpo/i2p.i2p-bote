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

import net.i2p.data.Destination;

public class RelayPeer extends Destination {
    private long requestsSent;
    private long responsesReceived;

    public RelayPeer(Destination destination, long requestsSent, long responsesReceived) {
        // initialize the Destination part of the RelayPeer
        setCertificate(destination.getCertificate());
        setSigningPublicKey(destination.getSigningPublicKey());
        setPublicKey(destination.getPublicKey());
        
        // initialize RelayPeer-specific fields
        this.requestsSent = requestsSent;
        this.responsesReceived = responsesReceived;
    }

    public long getRequestsSent() {
        return requestsSent;
    }

    public long getResponsesReceived() {
        return responsesReceived;
    }

    /**
     * Returns the percentage of requests sent to this peer for which
     * a response was received.
     * @return
     */
    public int getReachability() {
        if (requestsSent == 0)
            return 100;   // assume 100% reachability for new peers so the peer isn't removed before it has had a chance to respond to a request
        else
            return (int)(100L * responsesReceived / requestsSent);
    }
}