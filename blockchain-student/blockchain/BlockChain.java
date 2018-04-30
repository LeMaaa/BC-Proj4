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
    private Block newBlock;
    private Node node;
    private int id;

    public BlockChain(int id, Node node, int difficulty) {
        this.id = id;
        this.node = node;
        this.blocks = new ArrayList<>();
        this.difficulty = difficulty;
        this.target = computeTarget(difficulty);
        this.blocks.add(createGenesisBlock());
    }

    private BigInteger computeTarget(int difficulty) {
        return  BigInteger.valueOf(1).shiftLeft((256 - difficulty));
    }



    @Override
    public boolean addBlock(Block block) {
        System.out.println("Start add new block  --- ");
        if(isValidNewBlock(block, this.getLastBlock())){
            System.out.println("new block is validate --- ");
            this.blocks.add(block);
            return true;
        }
        System.out.println(" add new  block failed --- ");
        return false;
    }

    @Override
    public synchronized Block createGenesisBlock() {
        System.out.println("start to init  GB --" );
        String data = "This is Genesis Block!";
        String preHash = computePreHashForGB();
        System.out.println( "id : "+ id +  " -- preHash for GB --" + preHash);
        System.out.println("id : "+ id +  "----- ---- ----  --" );
        System.out.println("id : "+ id + "current Time  for GB --" +  System.currentTimeMillis());
        System.out.println("id : "+ id + "----- ---- ----  --" );
        Block genesisBlock = new Block(preHash, data, System.currentTimeMillis(), this.difficulty);
        String hashForGb = computeHashForGB(genesisBlock);
        genesisBlock.setHash(hashForGb);
        genesisBlock.setIndex(0);
        System.out.println("id : "+ id +  "GB is inited: +++  "  + genesisBlock);
        System.out.println("id : "+ id + "GB is Done----- ---- ----  --" );
        return genesisBlock;
    }

    private String computeHashForGB(Block gb) {
        String dataForHash = gb.getData();
        return DigestUtils.sha256Hex(dataForHash.getBytes());
    }

    private String computePreHashForGB() {
        String dummy = "This is previous hash for GB!";
        return DigestUtils.sha256Hex(dummy);
    }




    @Override
    public byte[] createNewBlock(String data) {
        System.out.println("Start create new block --- ");
        System.out.println("data is : " + data);
        String preHash =  getLastBlock().getHash();
        System.out.println("previuous hash for new block is  : " + preHash);
        Block newBlock = new Block(preHash, data, System.currentTimeMillis(), this.difficulty);
        newBlock.computePOW();
        newBlock.setIndex(getLastBlock().getIndex() + 1);
        this.newBlock = newBlock;
        byte[] newBlockByte = newBlock.toString().getBytes();
        System.out.println(" new block byte --- " );
        this.newBlockByte = newBlockByte;
        System.out.println(" new block is done --- ");
        return newBlockByte;
    }


    @Override
    // inside the broadcastNewBlock method in your BlockChainBase implementation,
    // you need to call the node’s broadcastNewBlockToPeer method
    public boolean broadcastNewBlock() {
        System.out.println(" start broadcaset to peers --- ");
        for(int i = 0; i < node.getPeerNumber(); i++) {
            if( i == this.id) continue;
            try {
                if(!node.broadcastNewBlockToPeer(i, this.newBlockByte)) {
                    return false;
                }
                System.out.println(" start broadcaset to peers ---  get false from peers");
            }catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        System.out.println(" all aggree on this block  --- ");

        this.blocks.add(this.newBlock);

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
        System.out.println(" call donwload blockchain --- ");
        String[] longestBlockChain = new String[0];
        int maxLen = 0;
        for(int i = 0; i < node.getPeerNumber(); i++) {
            if(i == this.id) continue;
            try {
                byte[] curByte = node.getBlockChainDataFromPeer(i);
                String curString = new String(curByte);
                System.out.println(" chain for peer --- " + i + "--" + curString);
                String[] peerBlocks = curString.split("@");
                if(peerBlocks.length > maxLen) {
                    maxLen = peerBlocks.length;
                    longestBlockChain = peerBlocks;
                    System.out.println(" longest bc when > --- " + maxLen);
                }else if(peerBlocks.length == maxLen) {
                    Block lastBlockForCurMax = Block.fromString(longestBlockChain[longestBlockChain.length - 1]);
                    Block lastBlockForPeer = Block.fromString(peerBlocks[peerBlocks.length - 1]);
                    // we want to choose the block containing the earliest timestamp.
//                    System.out.println(" longest bc ==  --- " + maxLen);
//                    System.out.println(" ts for currentMax ==  --- " + lastBlockForCurMax.getTimestamp());
//                    System.out.println(" ts for peer ==  --- " + lastBlockForPeer.getTimestamp());
                    if(lastBlockForCurMax.getTimestamp()
                            > lastBlockForPeer.getTimestamp()) {
                        longestBlockChain = peerBlocks;
                    }
                }
            }catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        System.out.println(" loop for broadcast down --- ");

        List<Block> curBlocks = new ArrayList<>();
        for(String s : longestBlockChain) {
            System.out.println(" string for data from longest peer --- " + s);
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
        System.out.println(" check ing validate in isValidateNewBlock  " );
        if(!newBlock.getPreviousHash().equals(prevBlock.getHash())
                || new BigInteger(newBlock.getHash(), 16).compareTo(target) != -1
                || newBlock.getIndex() != prevBlock.getIndex() + 1) {
            return false;
        }
        return true;
    }

    @Override
    public Block getLastBlock() {
//        if(this.getBlockChainLength() < 1) return null;

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
