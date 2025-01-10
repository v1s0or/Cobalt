package common;

import c2profile.Profile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.Serializable;
import java.security.KeyStore;

import net.jsign.DigestAlgorithm;
import net.jsign.PESigner;
import net.jsign.pe.PEFile;
import net.jsign.timestamp.TimestampingMode;

public class CodeSigner implements Serializable {
    protected byte[] keystore;

    protected String password = null;

    protected String alias = null;

    protected String digest_algorithm = null;

    protected String program_name = null;

    protected String program_url = null;

    protected boolean timestamp = false;

    protected String timestamp_url = null;

    protected String timestamp_mode = null;

    public CodeSigner() {
        this.keystore = new byte[0];
    }

    protected String get(Profile profile, String string) {
        if (profile.hasString(string) && !"".equals(profile.getString(string))) {
            return profile.getString(string);
        }
        return null;
    }

    public CodeSigner(Profile profile) {
        if (profile.isFile(".code-signer.keystore")) {
            this.keystore = CommonUtils.readFile(profile.getString(".code-signer.keystore"));
        } else {
            this.keystore = new byte[0];
            return;
        }
        this.password = profile.getString(".code-signer.password");
        this.alias = profile.getString(".code-signer.alias");
        this.digest_algorithm = get(profile, ".code-signer.digest_algorithm");
        this.program_name = get(profile, ".code-signer.program_name");
        this.program_url = get(profile, ".code-signer.program_url");
        this.timestamp_url = get(profile, ".code-signer.timestamp_url");
        this.timestamp_mode = get(profile, ".code-signer.timestamp_mode");
        this.timestamp = profile.option(".code-signer.timestamp");
    }

    public boolean available() {
        return (this.keystore.length > 0);
    }

    public byte[] sign(byte[] arrby) {
        if (!available())
            return arrby;
        String str = CommonUtils.writeToTemp("signme", "exe", arrby);
        sign(new File(str));
        byte[] datas = CommonUtils.readFile(str);
        (new File(str)).delete();
        return datas;
    }

    public void sign(File file) {
        if (!available()) {
            return;
        }
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new ByteArrayInputStream(this.keystore), this.password.toCharArray());
            PESigner pESigner = new PESigner(keyStore, this.alias, this.password);
            pESigner.withTimestamping(this.timestamp);
            if (this.program_name != null) {
                pESigner.withProgramName(this.program_name);
            }
            if (this.program_url != null) {
                pESigner.withProgramURL(this.program_url);
            }
            if (this.timestamp_mode != null) {
                pESigner.withTimestampingMode(TimestampingMode.valueOf(this.timestamp_mode));
            }
            if (this.timestamp_url != null) {
                pESigner.withTimestampingAutority(this.timestamp_url);
            }
            if (this.digest_algorithm != null) {
                pESigner.withDigestAlgorithm(DigestAlgorithm.valueOf(this.digest_algorithm));
            }
            pESigner.sign(new PEFile(file));
        } catch (Exception exception) {
            MudgeSanity.logException("Could not sign '" + file + "'", exception, false);
        }
    }
}
