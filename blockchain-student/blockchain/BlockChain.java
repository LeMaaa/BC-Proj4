import lib.Block;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Random;
import java.util.List;

import lib.POW;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Created by lema on 2018/4/26.
 */
public class BlockChain implements BlockChainBase {

    private Block genesisBlock;
    private List<Block> blocks;
    private int difficulty;
    private BigInteger target;
    private byte[] blockChain;
    private byte[] newBlockByte;
    private Node node;
    private int id;

    public BlockChain(int id, Node node, int difficulty) {
        this.id = id;
        this.node = node;
        this.blocks = new ArrayList<>();
        this.difficulty = difficulty;
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
        byte[] preHashByte = new byte[32];
        new Random().nextBytes(preHashByte);
        Block genesisBlock =  new Block(new String (preHashByte), data, System.currentTimeMillis(), this.difficulty);
        return genesisBlock;
    }




    @Override
    public byte[] createNewBlock(String data) {
        String preHash =  getLastBlock().getHash();
        Block newBlock = new Block(preHash, data, System.currentTimeMillis(), this.difficulty);
        newBlock.computePOW();
        byte[] newBlockByte = newBlock.toString().getBytes();
        this.newBlockByte = newBlockByte;
        return newBlockByte;
    }


    @Override
    // inside the broadcastNewBlock method in your BlockChainBase implementation,
    // you need to call the node’s broadcastNewBlockToPeer method
    public boolean broadcastNewBlock() {
        for(int i = 0; i < node.getPeerNumber(); i++) {
            if( i == this.id) continue;
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
        if(this.blocks == null || this.blocks.size() == 0) return new byte[0];
        StringBuilder sb = new StringBuilder();
        for(Block b : this.blocks) {
            sb.append(b.toString());
            sb.append("@");
        }
        return sb.toString().getBytes();
    }

    @Override
    // call getBlockChainDataFromPeer inside node class.
    public void downloadBlockchain() {
        String[] longestBlockChain = new String[0];
        int maxLen = 0;
        for(int i = 0; i < node.getPeerNumber(); i++) {
            if(i == this.id) continue;
            try {
                byte[] curByte = node.getBlockChainDataFromPeer(i);
                String curString = new String(curByte);
                String[] peerBlocks = curString.split("@");
                if(peerBlocks.length > maxLen) {
                    maxLen = peerBlocks.length;
                    longestBlockChain = peerBlocks;
                }else if(peerBlocks.length == maxLen) {
                    Block lastBlockForCurMax = Block.fromString(longestBlockChain[longestBlockChain.length - 1]);
                    Block lastBlockForPeer = Block.fromString(peerBlocks[peerBlocks.length - 1]);
                    // we want to choose the block containing the earliest timestamp.
                    if(lastBlockForCurMax.getTimestamp()
                            > lastBlockForPeer.getTimestamp()) {
                        longestBlockChain = peerBlocks;
                    }
                }
            }catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        List<Block> curBlocks = new ArrayList<>();
        for(String s : longestBlockChain) {
            curBlocks.add(Block.fromString(s));
        }
        this.blocks = curBlocks;
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
