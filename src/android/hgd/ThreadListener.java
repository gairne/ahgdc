package android.hgd;

import android.os.Message;

public interface ThreadListener {

	void notify(final int message, final String extraInfo);
}
