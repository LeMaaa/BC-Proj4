package lib;

/**
 * Created by lema on 2018/4/27.
 */
public class POW {
    Long nonce;
    String hash;

    public POW(Long nonce, String hash) {
        this.nonce = nonce;
        this.hash = hash;
    }

    public Long getNonce() {return nonce;}
    public String getHash() {return hash;}

}
