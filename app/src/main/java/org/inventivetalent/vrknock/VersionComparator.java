package org.inventivetalent.vrknock;

public class VersionComparator {

	public static final int OUTDATED_CLIENT = -1;// client version too old / server version too new
	public static final int SAME            = 0;
	public static final int OUTDATED_SERVER = 1;// server version too old / client version too new

	public static int compareVersions(String client, String server) {
		System.out.println("Client Version: " + client + ", Server Version: " + server);
		if (client == null || server == null || client.isEmpty() || server.isEmpty()) { return SAME; }
		int[] c = parseVersionString(client);
		int[] s = parseVersionString(server);
		// Major
		if (c[0] < s[0]) {
			return OUTDATED_CLIENT;
		} else if (s[0] < c[0]) {
			return OUTDATED_SERVER;
		} else {
			// Minor
			if (c[1] < s[1]) {
				return OUTDATED_CLIENT;
			} else if (s[1] < c[1]) {
				return OUTDATED_SERVER;
			}
		}
		return SAME;
	}

	static int[] parseVersionString(String string) {
		String[] split = string.split("\\.");
		int[] ints = new int[split.length];
		for (int i = 0; i < split.length; i++) {
			ints[i] = Integer.parseInt(split[i]);
		}
		return ints;
	}

}
