package cat.helm.tyrrel.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import cat.helm.tyrrel.BuildConfig;


public class SslHelper {
    private final Context context;
    private final SocketFactory socketFactory;

    public SslHelper(Context context) {
        this.context = context;
        socketFactory = newSslSocketFactory();
    }


    @NonNull
    private KeyStore getClientCertKeystore() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        /*** Client Certificate ***/

        KeyStore keyStore12 = KeyStore.getInstance("PKCS12");
        InputStream certInput12 = context.getAssets().open("keypair.p12");
        keyStore12.load(certInput12, BuildConfig.KEYSTORE_PASS.toCharArray());
        return keyStore12;
    }

    @NonNull
    private TrustManagerFactory getCATrustManagerFactory() throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate ca;
        try (InputStream caInput = new BufferedInputStream(context.getAssets().open("serverca.crt"))) {
            ca = cf.generateCertificate(caInput);
            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
        }

        // Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStoreCa = KeyStore.getInstance(keyStoreType);
        keyStoreCa.load(null, null);
        keyStoreCa.setCertificateEntry("ca", ca);
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStoreCa);
        return tmf;
    }

    private SSLSocketFactory newSslSocketFactory() {
        try {

            TrustManagerFactory caTrustManagerFactory = getCATrustManagerFactory();
            KeyStore clientCertKeystore = getClientCertKeystore();
            // Create a KeyManager that uses our client cert
            String algorithm = KeyManagerFactory.getDefaultAlgorithm();
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(algorithm);
            keyManagerFactory.init(clientCertKeystore, null);

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(keyManagerFactory.getKeyManagers(), caTrustManagerFactory.getTrustManagers(), null);

            return context.getSocketFactory();
        } catch (Exception e) {
            throw new AssertionError(e);
        }

    }

    public SocketFactory getSocketFactory() {
        return socketFactory;
    }
}
