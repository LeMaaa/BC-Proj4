import lib.Block;

import java.util.ArrayList;
import java.util.Random;
import java.util.List;

import lib.POW;
import org.apache.*;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Created by lema on 2018/4/26.
 */
public class BlockChain implements BlockChainBase {

    private Block genesisBlock;
    private List<Block> blocks;
    private int difficulty = 20;
    private byte[] blockChain;
    private Node node;
    private int id;

    public BlockChain(int id, Node node) {
        this.id = id;
        this.node = node;
        this.blocks = new ArrayList<>();
        this.genesisBlock = createGenesisBlock();
        this.blocks.add(this.genesisBlock);
    }



    @Override
    public boolean addBlock(Block block) {
        return false;
    }

    @Override
    public Block createGenesisBlock() {
        String data = "This is Genesis Block!";
        return  new Block(getLastBlock().getPreviousHash(), data, System.currentTimeMillis(), this.difficulty);
    }




    @Override
    public byte[] createNewBlock(String data) {
        String preHash =  getLastBlock().getHash();
        Block newBlock = new Block(preHash, data, System.currentTimeMillis(), this.difficulty);
        newBlock.computePOW();
    }


    @Override
    public boolean broadcastNewBlock() {
        return false;
        // inside the broadcastNewBlock method in your BlockChainBase implementation,
        // you need to call the node’s broadcastNewBlockToPeer method
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
    public void downloadBlockchain() {
        // call getBlockChainDataFromPeer inside node class.

    }

    @Override
    public void setNode(Node node) {
        this.node = node;
    }

    @Override
    public boolean isValidNewBlock(Block newBlock, Block prevBlock) {
        if(!newBlock.getPreviousHash().equals(prevBlock.getHash())
                || newBlock.getPow().get) {

        }
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
