package org.martus.android.network;

/**
 * Created by animal@martus.org on 6/16/15.
 */
public class Constants {
    private static final String IP_FOR_SL1_IE_REAL = "54.72.26.74";
    private static final String PUBLIC_KEY_FOR_SL1_IE_REAL =
            "MIIBojANBgkqhkiG9w0BAQEFAAOCAY8AMIIBigKCAYEAgzYTaocXQARAW5df4"
                    + "nvUYc6Sk2v9pQlMTB1v6/dc0nNamZAUaI5Z3ImPjnxCH/oATverq/Dsm8Gl"
                    + "MFOloHpXJwlPJyp3YUQ+wR9+MhhzG9qUsTNl6Iu8+f/GH6v6Sv1SXmUmS9E"
                    + "1jALpQqvCyBAbX+USyWo3P1uFmCYzlESPNoI8DUFCZ0XwTqQ3RmRrXYtVM9"
                    + "gIncknrcFwt14uf1UnVe0mIGyRUORGG3Pbl0hrMOopF2Ur/Z+bIFE535yF6"
                    + "Vpc+nFw+2nxBOpVgTvpt7LAtbxnxCzSO1KgAvUczBaQa4hXQ3dIlW//E9vK"
                    + "akQ85USbqXsxzr0scfkOxC7K+ZvYm0Porggn1W2b8dCGCUPNQAQRBFE7Czg"
                    + "b5EnmeumeJoLFon8El2idXRYcUBpY/FzHU4FM16guj85DWx7LEZ1LPFZXJv"
                    + "0u+DVd7KZfG4ovudn+ETKcskN4o6x/O6+KutVtTtIwmoIAam+lU/y8lZ+VC"
                    + "EqVxMiKkn2dp9nmvp780FOvAgMBAAE=";

    private static final String IP_FOR_SL1_DEV = "54.213.152.140";
    private static final String PUBLIC_KEY_SL1_DEV = "MIIBojANBgkqhkiG9w0BAQEFAAOCAY8AMIIBigKCAYEAjIX0yCfct1/WQptimL"
            + "jK35F3wsW/SEQ8DGdxfMBTZX1GVoOD6zg0d71Ns1ij4FdnOUsD4QCN4Kiay"
            + "Q+l28eIU8LL8L5oJClFwsVqgNDvPn8jR/CAbPy9NL0gKHevvX/dciVVCSrg"
            + "Oyyc9p9MP05qyekXqVIfLoZNkcXL5tQKrEiqVdJaDEPepPIkQpBgFwF0QZl"
            + "J7NdgF4T5wSyEt+fxL7qnZOCqchF8aVbSzAaGLRQEJEtFYTa9mOUCdCLtcn"
            + "sdgnj+lLftaV5+8o8ZeUTbyH5H/NlLddboxlI8rNalY7E5f3DltOOmTyjMh"
            + "KSaxl9lfIxpfKoeLdYb5bA74BV1AjbwnxahlN4KRZm/7i0RkapKIXZ0Hqus"
            + "4JKUG5CJcIybS64ppt8ufCvAEERrZUzrrIDNwv+qob9PYFdiMq1xg+VNrxm"
            + "/0RXfjwgXxNjDS07MTQc2w/z1egtsDLSi4dALw69nefS0hbZwbv8dIrN23i"
            + "Hn0FNdbz81l1FrELGyh1hRAgMBAAE=";


    public static String getCurrentServerIp()
    {
        return IP_FOR_SL1_IE_REAL;
    }

    public static String getCurrentSeverKey()
    {
        return PUBLIC_KEY_FOR_SL1_IE_REAL;
    }
}
