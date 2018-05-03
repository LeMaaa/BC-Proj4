package lib;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Random;

/**
 * Block Class, the element to compose a Blockchain.
 */
public class Block implements Serializable{

    private String hash;
    private String previousHash;
    private String data;
    private long timestamp;
    private int difficulty;
    private long nonce;
    private BigInteger target;
    private POW pow;
    private int index;

    private static final long serialVersionUID = 1;

    public Block() {}

    public Block( String previousHash, String data,
                 long timestamp, int difficulty) {
        this.previousHash = previousHash;
        this.data = data;
        this.timestamp = timestamp;
        this.difficulty = difficulty;
        this.nonce = 0;
        this.target = computeTarget(difficulty);
    }

    public Block( String hash, String previousHash, String data,
                  long timestamp, int difficulty, int nonce) {
        this.hash = hash;
        this.previousHash = previousHash;
        this.data = data;
        this.timestamp = timestamp;
        this.difficulty = difficulty;
        this.nonce = nonce;
        this.target = computeTarget(difficulty);
    }

    private BigInteger computeTarget(int difficulty) {
        return  BigInteger.valueOf(1).shiftLeft((256 - difficulty));

    }

    public String prepareData(long nonce) {
        String preHash = this.previousHash;
        String curData = this.data;
        if( preHash == null || preHash.equals(" ")  || preHash.length() == 0) {
            byte[] preHashByte = new byte[32];
            new Random().nextBytes(preHashByte);
            preHash = new String (preHashByte);
        }

        if( curData == null || curData.equals("") || curData.length() == 0) {
            byte[] dataByte = new byte[32];
            new Random().nextBytes(dataByte);
            curData = new String (dataByte);
        }

        return preHash + curData + Long.toString(this.timestamp) + difficulty + nonce;
    }

    public void computePOW() {
        long index = 0;
        String curHash = "";
        System.out.println("Start mine: --- " + index);
        while(index < Long.MAX_VALUE) {
            byte[] curData = prepareData(index).getBytes();
            curHash = DigestUtils.sha256Hex(curData);
            if(new BigInteger(curHash, 16).compareTo(this.target) == -1) {
                System.out.println("Found Hash: ---  " + curHash);
                System.out.println("Found Nonce: ---  " + index);
                break;
            }else {
                index++;
            }
        }
        this.pow = new POW(index,curHash);
        this.setHash(curHash);
        this.setNonce(index);
    }

    public POW getPow() { return this.pow; }

    public long getNonce() { return nonce; }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getData() {
        return data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setIndex(int index) { this.index = index;}

    public int getIndex() {return this.index;}


    public static Block fromString(String s){
        String[] arr = s.split("#");
        Block block = new Block(arr[0], arr[1], arr[2], Long.parseLong(arr[3]), Integer.parseInt(arr[4]), Integer.parseInt(arr[5]));
        block.setIndex(Integer.parseInt(arr[6]));
        return block;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        // arr[0]  preHash
        sb.append(this.hash).append("#");
        // arr[1]  curHash
        sb.append(this.previousHash).append("#");
        // arr[2]  data
        sb.append(this.data).append("#");
        // arr[3]  timestamp
        sb.append(this.timestamp).append("#");
        // arr[4]  difficulty
        sb.append(this.difficulty).append("#");
        // arr[5]   nonce
        sb.append(this.nonce).append("#");
        // arr[6]   index
        sb.append(this.index);
        return sb.toString();
    }

}
