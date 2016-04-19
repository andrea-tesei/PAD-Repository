package it.cnr.isti.pad.UDPSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import com.google.code.gossip.GossipService;

import it.cnr.isti.pad.PADfs.App;


public class UDPServer implements IUDPSocket {
	private final int localPort = 9099;
	private String serverName = "";
	private DatagramSocket dgsocket = null;
	
	public UDPServer() throws UnknownHostException, SocketException {
		this.serverName = App.grs.getGossipService().get_gossipManager().getMyself().getHost();
		System.out.println("Starting UDP server at: " + this.serverName);
		this.dgsocket = new DatagramSocket(this.localPort);
	}
	
	@Override
	public boolean sendPacket(byte[] msg, SocketAddress addr) {
		try {
			// Transforming length of the packet in byte-encoding
			byte[] length_bytes = new byte[4];
			length_bytes[0] = (byte) (msg.length >> 24);
			length_bytes[1] = (byte) ((msg.length << 8) >> 24);
			length_bytes[2] = (byte) ((msg.length << 16) >> 24);
			length_bytes[3] = (byte) ((msg.length << 24) >> 24);

			// Sending packet
			ByteBuffer byteBuffer = ByteBuffer.allocate(4 + msg.length);
			byteBuffer.put(length_bytes);
			byteBuffer.put(msg);
			byte[] buf = byteBuffer.array();
			this.dgsocket.send(new DatagramPacket(buf, buf.length, addr));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	@Override
	public String receivePacket() {
		String receivedMessage = "";
		DatagramPacket p = null;
		try {
			byte[] buf = new byte[this.dgsocket.getReceiveBufferSize()];
			p = new DatagramPacket(buf, buf.length);
			this.dgsocket.receive(p);

			// Retrieving packet length
			int packet_length = 0;
			for (int i = 0; i < 4; i++) {
				int shift = (4 - 1 - i) * 8;
				packet_length += (buf[i] & 0x000000FF) << shift;
			}
			
			// Read content of the message
			byte[] json_bytes = new byte[packet_length];
			for (int i = 0; i < packet_length; i++) {
				json_bytes[i] = buf[i + 4];
			}
			receivedMessage = new String(json_bytes);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return receivedMessage;
	}
	
	@Override
	public boolean closeConnection(){
		this.dgsocket.close();
		return this.dgsocket.isClosed();
	}
	
	public int getLocalPort() {
		return localPort;
	}

	public String getServerName() {
		return serverName;
	}

	public DatagramSocket getDgsocket() {
		return dgsocket;
	}
}
