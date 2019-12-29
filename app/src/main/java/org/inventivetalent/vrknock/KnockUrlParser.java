package org.inventivetalent.vrknock;

import android.net.Uri;

import androidx.annotation.Nullable;

import java.util.List;

public class KnockUrlParser {

	@Nullable
	public static ParsedKnockInfo parse(Uri uri) {
		if(uri==null)return null;
		ParsedKnockInfo knockInfo = new ParsedKnockInfo();

		String cm = uri.getQueryParameter("cm");
		if (cm != null && cm.length() > 0) {
			knockInfo.connectionMethod = ConnectionMethod.valueOf(cm.trim().toUpperCase());
		}

		List<String> segments = uri.getPathSegments();
		if (segments.size() == 0) { return null; }
		if ("vrknock.app".equals(uri.getHost())) {/// http(s)://vrknock.app/1.2.3.4/code OR http(s)://vrknock.app/a-unique-server-id/code
			knockInfo.host = segments.get(0);
			knockInfo.code = segments.get(1);
		} else {/// http://1.2.3.4/code
			knockInfo.host = uri.getHost();
			knockInfo.code = segments.get(0);
		}

		return knockInfo;
	}

	public static class ParsedKnockInfo {
		public String           host             = "";
		public String           code             = "";
		public ConnectionMethod connectionMethod = ConnectionMethod.DIRECT;

		@Override
		public String toString() {
			return "ParsedKnockInfo{" +
					"host='" + host + '\'' +
					", code='" + code + '\'' +
					", connectionMethod=" + connectionMethod +
					'}';
		}
	}

}
