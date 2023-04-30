package com.example.partsmaintenance;

import java.io.*;
import java.util.*;

/**
 * @author: Xiaoyang Yu
 * @create_at: 2023/4/23 7:43
 * @version: 1.0
 */
public class BPlusTree<K extends Comparable<K>, V> {

    //root
    private BPlusTreeNode root;
    //head
    private BPlusTreeNode head;
    //tail
    private BPlusTreeNode tail;

    private int treeSize;
    //height
    private int treeHeight;
    //tree degree
    private int treeDegree;
    //UPPER_BOUND （M -1 ）   UNDER_BOUND（M-1）/2
    private int UPPER_BOUND;
    private int UNDER_BOUND;

    private int leafSplitCount;
    private int parentSplitCount;

    private int fusionsCount;
    private int parentFusionCount;

    //Constructor
    public BPlusTree(int treeDegree) {
        if (treeDegree < 3) {
            throw new IllegalArgumentException("The degree cannot be less than three！");
        }
        this.treeDegree = treeDegree;
        this.UPPER_BOUND = treeDegree - 1;
        this.UNDER_BOUND = UPPER_BOUND / 2;
    }

    //
    public void put(K key, V value) {
        if (key == null) {
            return;
        }
        //The root node is empty, enter a new one, head node = tail node = root, size and height = 1
        if (root == null) {
            root = new BPlusTreeData(toList(key), toList(value));
            head = tail = root;
            treeSize++;
            treeHeight = 1;
            return;
        }
        // Add child nodes If the root node is not empty, call the put() method of the root node to insert key-value pairs.
        BPlusTreeNode newChildren = root.put(key, value);
        if (newChildren != null) {
            K leafKey = newChildren.findLeafKey();
            root = new BPlusTreeIndex(toList(leafKey), toList(root, newChildren));
            treeHeight++;
        }
    }

    /**
     * Function description: get() method
     *
     * @auther: Xiaoyang Yu
     */
    public V get(K key) {
        if (root == null || key == null) {
            return null;
        }
        return root.get(key);
    }

    public Map<K,V> rangeQuery(K start, K end){

        return root.rangeQuery( start, end);

    }
    public void remove(K key) {
        if (root == null || key == null) {
            return;
        }
        DeleteInfo deleteInfo = root.remove(key);
        if (!deleteInfo.isDelete) {
            return;
        }
        if (root.keys.isEmpty()) {
            if (root.getClass().equals(BPlusTreeData.class)) {
                root = null;
            } else {
                root = ((BPlusTreeIndex) root).children.get(0);
            }
            treeHeight--;
        }
    }

    /**
     * Function description: containsKey() method Determines whether there is a key corresponding to
     *                       the specified key value in the B+ tree。
     * @auther: Xiaoyang Yu
     */
    public boolean containsKey(K key) {
        V v = get(key);
        if (v == null) {
            return false;
        }
        return true;
    }

    //
    public <T> List<T> toList(T... t) {
        List<T> list = new ArrayList<>();
        Collections.addAll(list, t);
        return list;
    }

    public K floorKey(K key) {
        if (key == null || root == null) {
            return null;
        }
        return root.floorKey(key);
    }

    public K ceilingKey(K key) {
        if (key == null || root == null) {
            return null;
        }
        return root.ceilingKey(key);
    }

    public K firstKey() {
        return head.keys.get(0);
    }

    public K lastKey() {
        return tail.keys.get(tail.keys.size() - 1);
    }

    public int size() {
        return treeSize;
    }

    public int height() {
        return treeHeight;
    }

    public int leafSplitCount() {
        return leafSplitCount;
    }
    public int parentSplitCount() {
        return parentSplitCount;
    }
    public int getFusionsCount(){
        return fusionsCount;
    }
    public int getParentFusionCount(){
        return parentFusionCount;
    }
    public void clear() {
        root = null;
        head = null;
        tail = null;
        treeSize = 0;
        treeHeight = 0;
        leafSplitCount = 0;
        parentSplitCount = 0;
        fusionsCount = 0;
        parentFusionCount = 0;
    }
    /**************************Node Class*********************************/
    /**
     * Function description: This code defines an abstract class BPlusTreeNode, which represents a node in the B+ tree.
     * Among them, keys is a list of storage keys, and put() is an abstract method for inserting a key-value pair into a node.
     * In addition, there are three protected methods, namely findUpperIndex(), getMidIndex() and findLeafKey()
     *
     * @auther: Xiaoyang Yu
     */
    public abstract class BPlusTreeNode {
        //key
        protected List<K> keys;

        /**
         * Function description: put() method
         *
         * @auther: Xiaoyang Yu
         */

        public abstract BPlusTreeNode put(K key, V value);

        /**
         * Function description: get() method
         *
         * @auther: Xiaoyang Yu
         */

        public abstract V get(K key);

        /**
         * Function description: remove() method
         *
         * @auther: Xiaoyang Yu
         */

        /**
         * Function description: rangeQuery()
         * @auther: Xiaoyang Yu
         */
        public abstract Map<K,V> rangeQuery(K start, K end);

        public abstract DeleteInfo remove(K key);

        /**
         * Function description: When deleting, you need to find the left or right fusion.
         *
         * @auther: Xiaoyang Yu
         */
        public abstract void borrow(BPlusTreeNode brother, boolean isLeft, K parentKey);

        /**
         * Function description: merge method
         *
         * @auther: Xiaoyang Yu
         */
        protected abstract void combine(BPlusTreeNode childNode, K parentKey);

        /**
         * Function description: The findLeafKey() method is used to find the key of a leaf in a node.
         * If the node itself is a leaf, directly return the first key of the node.
         * If the node is not a leaf, recursively find the child nodes of the node until a leaf is found,
         * and then return the first key of the leaf.
         * @auther: Xiaoyang Yu
         */
        protected abstract K findLeafKey();

        public abstract K floorKey(K key);

        public abstract K ceilingKey(K key);

        /**
         * Function description: The findUpperIndex() method is used to find the insertion position of a key in a node.
         * This method is implemented using a binary search algorithm. Specifically,
         * it will keep narrowing down the search interval until it finds the position of the last key less than or equal to key,
         * then return the position to the right of the position, which is the insertion position of the key
         *
         * @auther: Xiaoyang Yu
         */
        protected int findUpperIndex(K key) {
            int left = 0;
            int right = keys.size();
            while (left < right) {
                int mid = left + ((right - left) >> 1);
                if (key.compareTo(keys.get(mid)) >= 0) {
                    left = mid + 1;
                } else {
                    right = mid;
                }
            }
            return left;
        }

        protected int findCeilingKeyIndex(K key) {
            int left = 0;
            int right = keys.size() - 1;
            int resIndex = keys.size();
            while (left <= right) {
                int mid = left + ((right - left) >> 1);
                if (key.compareTo(keys.get(mid)) <= 0) {
                    resIndex = mid;
                    right = mid - 1;
                } else {
                    left = mid + 1;
                }
            }
            return resIndex;
        }

        /**
         * Function description: The getMidIndex() method returns the middle position of a node,
         * which is used when splitting the node into two nodes.
         *
         * @auther: Xiaoyang Yu
         */
        protected int getMidIndex() {
            return UPPER_BOUND / 2;
        }

    }

    /*********************************BPlusTreeDataNode*************************************/
    //leaf node, nodes that store data
    public class BPlusTreeData extends BPlusTreeNode {

        // Holds the values (i.e. data) of all key-value pairs stored by this node.
        public List<V> treeData;
        //Pointer to the next leaf node.
        private BPlusTreeData nextPointer;
        //Pointer to the previous leaf node.
        private BPlusTreeData prePointer;


        public BPlusTreeData(List<K> keys, List<V> treeData) {
            this.keys = keys;
            this.treeData = treeData;
        }

        /**
         * Function description: The put method is the concrete realization of the abstract method of BPlusTreeNode.
         * It is used to add a key-value pair in the current node. If a key equal to key already exists,
         * update the value corresponding to the key;
         * Otherwise, insert the new key and new value in place.
         * If the node size exceeds UPPER_BOUND after insertion, call the split method to split the node.
         * If it is a leaf node after splitting, return the new right node, otherwise return null.
         *
         * @auther: Xiaoyang Yu
         */
        @Override
        public BPlusTreeData put(K key, V value) {
            int equalKeyIndex = findEqualKeyIndex(key);
            if (equalKeyIndex != -1) {
                treeData.set(equalKeyIndex, value);
                return null;
            }
            treeSize++;
            int upperIndex = findUpperIndex(key);
            keys.add(upperIndex, key);
            treeData.add(upperIndex, value);

            //分裂
            if (keys.size() > UPPER_BOUND) {
                BPlusTreeData split = split();
                leafSplitCount++;
                return split;
            }
            return null;
        }

        @Override
        public V get(K key) {
            int equalKeyIndex = findEqualKeyIndex(key);
            if (equalKeyIndex != -1) {
                return treeData.get(equalKeyIndex);
            }
            return null;
        }
        @Override
        public Map<K, V> rangeQuery(K start, K end) {
            HashMap<K, V> res = new HashMap<>();
            int startIndex = findCeilingKeyIndex(start);
            int endIndex = findCeilingKeyIndex(end);
            for (int i = startIndex; i < endIndex ; i++) {
                res.put(keys.get(i),treeData.get(i));
            }
            BPlusTreeData next = this.nextPointer;
            while (next !=null){
                for (int i = 0; i < next.keys.size(); i++) {
                    if(next.keys.get(i).compareTo(end) < 0){
                        res.put(next.keys.get(i),next.treeData.get(i));
                    }else {
                        return res;
                    }
                }
                next=next.nextPointer;
            }
            return res;
        }

        @Override
        public DeleteInfo remove(K key) {
            int equalKeyIndex = findEqualKeyIndex(key);
            if (equalKeyIndex == -1) {
                return new DeleteInfo(false, false);
            }
            treeSize--;
            this.keys.remove(equalKeyIndex);
            this.treeData.remove(equalKeyIndex);

            return new DeleteInfo(true, keys.size() < UNDER_BOUND);
        }

        /**
         * Function description: data node borrow() method
         *
         * @auther: Xiaoyang Yu
         */
        @Override
        public void borrow(BPlusTreeNode brother, boolean isLeft, K parentKey) {
            BPlusTreeData brotherNode = (BPlusTreeData) brother;
            if (isLeft) {
                this.keys.add(0, brotherNode.keys.remove(brotherNode.keys.size() - 1));
                this.treeData.add(0, brotherNode.treeData.remove(brotherNode.keys.size() - 1));
            } else {
                this.keys.add(0, brotherNode.keys.remove(0));
                this.treeData.add(0, brotherNode.treeData.remove(0));
            }
        }

        /**
         * Function description: this is the left brother
         * childNode is the node whose key is currently deleted
         *
         * @auther: Xiaoyang Yu
         */
        @Override
        protected void combine(BPlusTreeNode childNode, K parentKey) {
            BPlusTreeData brotherNode = (BPlusTreeData) childNode;
            this.keys.addAll(brotherNode.keys);
            this.treeData.addAll(brotherNode.treeData);

            if (brotherNode.nextPointer == null) {
                tail = this;
            } else {
                brotherNode.nextPointer.prePointer = this;
            }
            this.nextPointer = brotherNode.nextPointer;
        }

        @Override
        protected K findLeafKey() {
            return keys.get(0);
        }

        /**
         * Function description: Specifically, a modified binary search method is used here.
         * Since what is to be found is the maximum value less than or equal to the key, when the key is greater than or equal to the element corresponding to mid,
         * set left to mid, otherwise set right to mid-1.
         * The loop condition is left < right, when left and right are equal,
         * exit the loop. This ensures that the element pointed to by the final left is less than or equal to the maximum value of key.
         * If the element is less than or equal to key,
         * then return the element, otherwise move forward a pointer to find the largest element of the predecessor node
         *
         * @auther: Xiaoyang Yu
         */
        @Override
        public K floorKey(K key) {
            int left = 0;
            int right = keys.size() - 1;
            while (left < right) {
                int mid = (left + right + 1) / 2;
                if (key.compareTo(keys.get(mid)) >= 0) {
                    left = mid;
                } else {
                    right = mid - 1;
                }
            }
            if (key.compareTo(keys.get(left)) >= 0) {
                return keys.get(left);
            }
            BPlusTreeData pre = this.prePointer;
            if (pre == null) {
                return null;
            }
            return pre.keys.get(pre.keys.size() - 1);
        }

        @Override
        public K ceilingKey(K key) {

            int left = 0;
            int right = keys.size() - 1;
            while (left < right) {
                int mid = (left + right) / 2;
                if (key.compareTo(keys.get(mid)) > 0) {
                    left = mid + 1;
                } else {
                    right = mid;
                }
            }
            if (key.compareTo(keys.get(right)) <= 0) {
                return keys.get(right);
            }
            BPlusTreeData next = this.nextPointer;
            if (next == null) {
                return null;
            }
            return next.keys.get(0);
        }

        //分裂
        private BPlusTreeData split() {
            int midIndex = getMidIndex();
            List<K> allKeys = keys;
            List<V> allData = treeData;

            this.keys = new ArrayList<>(allKeys.subList(0, midIndex));
            this.treeData = new ArrayList<>(allData.subList(0, midIndex));

            List<K> rightKeys = new ArrayList<>(allKeys.subList(midIndex, allKeys.size()));
            List<V> rightData = new ArrayList<>(allData.subList(midIndex, allData.size()));

            BPlusTreeData rightNode = new BPlusTreeData(rightKeys, rightData);
            if (this.nextPointer == null) {
                tail = rightNode;
            }
            rightNode.nextPointer = this.nextPointer;
            if (this.nextPointer != null) {
                this.nextPointer.prePointer = rightNode;
            }
            this.nextPointer = rightNode;
            rightNode.prePointer = this;

            return rightNode;
        }

        /**
         * Function description: In each round of binary search, the middle position mid of the current search range is obtained by calculation,
         * and the size of key and the key value whose position is mid in the list is compared.
         * If they are equal, it means that the target position has been found, and mid is returned directly.
         * If the key is smaller than the key value in the middle position, it means that the target position is to the left of mid,
         * and the search range needs to be narrowed down to [left, mid-1].
         * If the key is greater than the key value in the middle position,
         * it means that the target position is on the right side of mid, and the search range needs to be narrowed down to [mid+1, right]
         *
         * @auther: Xiaoyang Yu
         */
        private int findEqualKeyIndex(K key) {
            int left = 0;
            int right = keys.size() - 1;
            while (left <= right) {
                int mid = left + ((right - left) >> 1);
                if (key.compareTo(keys.get(mid)) == 0) {
                    return mid;
                } else if (key.compareTo(keys.get(mid)) < 0) {
                    right = mid - 1;
                } else {
                    left = mid + 1;
                }
            }
            return -1;
        }
    }

    /*********************non-leaf node(No data)*****************************************/
    public class BPlusTreeIndex extends BPlusTreeNode {

        public List<BPlusTreeNode> children;

        public BPlusTreeIndex(List<K> keys, List<BPlusTreeNode> children) {
            this.keys = keys;
            this.children = children;
        }

        @Override
        public BPlusTreeNode put(K key, V value) {
            int upperIndex = findUpperIndex(key);
            BPlusTreeNode bPlusTreeNode = children.get(upperIndex).put(key, value);
            if (null == bPlusTreeNode) {
                return null;
            }
            K newKey = bPlusTreeNode.findLeafKey();

            int newKeyIndex = findUpperIndex(newKey);
            keys.add(newKeyIndex, newKey);
            children.add(newKeyIndex + 1, bPlusTreeNode);
            if (keys.size() > UPPER_BOUND) {
                BPlusTreeIndex split = split();
                split.keys.remove(0);
                parentSplitCount++;
                return split;
            }
            return null;
        }

        @Override
        public V get(K key) {
            int upperIndex = findUpperIndex(key);
            return children.get(upperIndex).get(key);
        }
        @Override
        public Map<K, V> rangeQuery(K start, K end) {
            int upperIndex = findUpperIndex(start);
            return children.get(upperIndex).rangeQuery(start,end);
        }

        @Override
        public DeleteInfo remove(K key) {
            int childIndex = findUpperIndex(key);
            BPlusTreeNode childNode = children.get(childIndex);
            DeleteInfo deleteInfo = children.get(childIndex).remove(key);

            //如果没有删除
            if (!deleteInfo.isDelete) {
                return deleteInfo;
            }
            //如果低于下界
            if (deleteInfo.isUnder) {
                delete_maintain(childNode, childIndex);
                parentFusionCount++;
            }
            return new DeleteInfo(true, keys.size() < UNDER_BOUND);
        }

        private void delete_maintain(BPlusTreeNode childNode, int childIndex) {
            int parentKeyIndex = Math.max(0, childIndex - 1);
            if (childIndex > 0 && children.get(childIndex - 1).keys.size() > UNDER_BOUND) {
                //左边
                BPlusTreeNode leftBrotherNode = children.get(childIndex - 1);

                K parentKey = keys.get(parentKeyIndex);

                childNode.borrow(leftBrotherNode, true, parentKey);  //这里调用意味着来到了leaf节点
                K leafKey = childNode.findLeafKey();
                this.keys.set(parentKeyIndex, leafKey);

            } else if (childIndex < children.size() - 1 && children.get(childIndex + 1).keys.size() > UNDER_BOUND) {
                //右边
                BPlusTreeNode rightBrother = children.get(childIndex + 1);
                parentKeyIndex= childIndex==0?0:Math.min(keys.size()-1,parentKeyIndex+1);
                K parentKey = keys.get(parentKeyIndex);
                childNode.borrow(rightBrother,false,parentKey);
                //
                K leafKey = rightBrother.findLeafKey();
                //
                this.keys.set(parentKeyIndex,leafKey);
            } else {
                //大家都穷
                if(childIndex > 0){
                    BPlusTreeNode leftBrother = children.get(childIndex - 1);
                    K parentKey = keys.get(parentKeyIndex);
                    leftBrother.combine(childNode,parentKey);
                    this.keys.remove(parentKeyIndex);
                    //
                    this.children.remove(childIndex);
                }else {
                    //来到这里，那么childNode一定是第一个孩子 下标为0
                    BPlusTreeNode rightBrother = children.get(childIndex+1);
                    //BPlusTreeNode rightBrother = children.get(1);
                    childNode.combine(rightBrother,this.keys.get(0));
                    this.keys.remove(0);
                    //问题1
                    this.children.remove(1);
                }
            }
        }

        //非叶子节点
        @Override
        public void borrow(BPlusTreeNode brother, boolean isLeft, K parentKey) {
            BPlusTreeIndex brotherNode = (BPlusTreeIndex) brother;
            if (isLeft) {
                this.keys.add(0, parentKey);
                this.children.add(0, brotherNode.children.remove(brotherNode.children.size() - 1));
                brotherNode.keys.remove(brotherNode.keys.size() - 1);
            } else {
                this.keys.add(parentKey);
                this.children.add(brotherNode.children.remove(0));
                brotherNode.keys.remove(0);
            }
        }

        @Override
        protected void combine(BPlusTreeNode brother, K parentKey) {
            BPlusTreeIndex brotherNode=(BPlusTreeIndex)brother;
            this.keys.add(parentKey);
            this.keys.addAll(brotherNode.keys);
            this.children.addAll(brotherNode.children);
        }

        //非叶子节点分裂
        private BPlusTreeIndex split() {
            int midIndex = getMidIndex();
            List<K> allKeys = keys;
            List<BPlusTreeNode> allChildren = children;

            this.keys = new ArrayList<>(allKeys.subList(0, midIndex));
            this.children = new ArrayList<>(allChildren.subList(0, midIndex + 1));

            List<K> rightKeys = new ArrayList<>(allKeys.subList(midIndex, allKeys.size()));
            List<BPlusTreeNode> rightChildren = new ArrayList<>(allChildren.subList(midIndex + 1, allChildren.size()));
            return new BPlusTreeIndex(rightKeys, rightChildren);
        }

        @Override
        protected K findLeafKey() {
            return children.get(0).findLeafKey();
        }

        @Override
        public K floorKey(K key) {
            int upperIndex = findUpperIndex(key);
            return children.get(upperIndex).floorKey(key);
        }

        @Override
        public K ceilingKey(K key) {
            int upperIndex = findUpperIndex(key);
            return children.get(upperIndex).ceilingKey(key);
        }
    }

    /**
     * Class description: Mainly return deletion information
     *
     * @auther: Xiaoyang Yu
     */
    public class DeleteInfo {
        public boolean isDelete;   //Is it really deleted
        public boolean isUnder;    //Is it below the lower bound

        public DeleteInfo(boolean isDelete, boolean isUnder) {
            this.isDelete = isDelete;
            this.isUnder = isUnder;
        }
    }

/*    public static void main(String[] args) throws IOException {

        BPlusTree<String, String> bptree = new BPlusTree(5);

        // 读取文件
        String encoding = "utf-8";
        File file = new File("C:\\Users\\46515\\Desktop\\partfile.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
        String line = reader.readLine();
        while (line != null) {
            String resultKey = line.substring(0, 7);
            String resultValue = line.substring(15);
            //String[] data = line.split("\\s+");
            //String key = data[0];
            // String value = data[1] + " " + data[2] + " " + data[3];
            bptree.put(resultKey, resultValue);
            line = reader.readLine();  // 读取下一行数据
        }
        reader.close();
        System.out.println(bptree.get("ABQ-289"));
        System.out.println("B+树的高度为：" + bptree.height());

    }*/

}
