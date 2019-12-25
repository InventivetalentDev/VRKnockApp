package org.inventivetalent.vrknock;

import android.content.Context;
import android.net.Uri;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
	@Test
	public void useAppContext() {
		// Context of the app under test.
		Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

		assertEquals("org.inventivetalent.vrknock", appContext.getPackageName());
	}

	@Test
	public void oldUrlTest() {
		String url = "http://192.168.178.45/acode";
		Uri uri = Uri.parse(url);
		assertEquals("192.168.178.45", uri.getHost());
		assertEquals("acode",uri.getPathSegments().get(0));
	}

	@Test
	public void newUrlTest() {
		String url = "https://vrknock.app/192.168.178.45/acode";
		Uri uri = Uri.parse(url);
		assertEquals("192.168.178.45", uri.getPathSegments().get(0));
		assertEquals("acode",uri.getPathSegments().get(1));
	}

}
