import lib.Block;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Random;
import java.util.List;

import lib.POW;
import org.apache.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.SerializationUtils;

/**
 * Created by lema on 2018/4/26.
 */
public class BlockChain implements BlockChainBase {

    private Block genesisBlock;
    private List<Block> blocks;
    private int difficulty = 20;
    private BigInteger target;
    private byte[] blockChain;
    private byte[] newBlockByte;
    private Node node;
    private int id;

    public BlockChain(int id, Node node) {
        this.id = id;
        this.node = node;
        this.blocks = new ArrayList<>();
        this.target = computeTarget(difficulty);
        this.genesisBlock = createGenesisBlock();
        this.blocks.add(this.genesisBlock);
    }

    private BigInteger computeTarget(int difficulty) {
        return  BigInteger.valueOf(1).shiftLeft((256 - difficulty));
    }



    @Override
    public boolean addBlock(Block block) {
        return false;
    }

    @Override
    public Block createGenesisBlock() {
        String data = "This is Genesis Block!";
        return new Block(getLastBlock().getPreviousHash(), data, System.currentTimeMillis(), this.difficulty);
    }




    @Override
    public byte[] createNewBlock(String data) {
        String preHash =  getLastBlock().getHash();
        Block newBlock = new Block(preHash, data, System.currentTimeMillis(), this.difficulty);
        newBlock.computePOW();
        byte[] newBlockByte = SerializationUtils.serialize(newBlock);
        this.newBlockByte = newBlockByte;
        return newBlockByte;
    }


    @Override
    // inside the broadcastNewBlock method in your BlockChainBase implementation,
    // you need to call the node’s broadcastNewBlockToPeer method
    public boolean broadcastNewBlock() {
        for(int i = 0; i < node.getPeerNumber(); i++) {
            try {
                if(!node.broadcastNewBlockToPeer(i, this.newBlockByte)) {
                    return false;
                }
            }catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public byte[] getBlockchainData() {
        return new byte[0];
    }

    @Override
    // call getBlockChainDataFromPeer inside node class.
    public void downloadBlockchain() {
        byte[] longestBlockChain = null;
        long maxLen = 0;
        for(int i = 0; i < node.getPeerNumber(); i++) {
            try {
                byte[] cur = node.getBlockChainDataFromPeer(i);
                if(cur.length > maxLen) {
                    maxLen = cur.length;
                    longestBlockChain = cur;
                }else if(cur.length == maxLen) {
                    List<Block> preLongestBlockChain = SerializationUtils.deserialize(longestBlockChain);
                    List<Block> curBlockChain = SerializationUtils.deserialize(cur);
                    // we want to choose the block containing the earliest timestamp.
                    if(preLongestBlockChain.get(preLongestBlockChain.size() - 1).getTimestamp()
                            > curBlockChain.get(curBlockChain.size() - 1).getTimestamp()) {
                        longestBlockChain = cur;
                    }
                }
            }catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void setNode(Node node) {
        this.node = node;
    }

    @Override
    public boolean isValidNewBlock(Block newBlock, Block prevBlock) {
        if(!newBlock.getPreviousHash().equals(prevBlock.getHash())
                || new BigInteger(newBlock.getPow().getHash(), 16).compareTo(target) != -1) {
            return false;
        }
        return true;
    }

    @Override
    public Block getLastBlock() {
        if(this.getBlockChainLength() < 1) return null;

        return this.getBlocks().get(getBlockChainLength() - 1);
    }

    private List<Block> getBlocks() { return this.blocks;}

    public Block getGenesisBlock() {return this.genesisBlock;}

    @Override
    public int getBlockChainLength() {
        if(this.blocks == null || this.blocks.size() == 0) {
            return 0;
        }
        return this.blocks.size();
    }
}
