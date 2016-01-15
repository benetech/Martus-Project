package org.martus.android;

import org.martus.common.network.NetwordResponseHander;
import org.martus.common.network.PublicKeyTaskPostExecuteHandler;

/**
 * Created by nimaa on 4/3/14.
 */
abstract public class AbstractServerActivity extends BaseActivity implements NetwordResponseHander, PublicKeyTaskPostExecuteHandler {
    public static final int MIN_SERVER_CODE = 20;
    public static final String SERVER_INFO_FILENAME = "Server.mmsi";
}
