package tagger;

import java.util.*;

public class Knapsack implements TaskExecuter {
    final static int debug = 0;
    // percentual to consider a container to be totally filled!
    final static float totallyfilled = (float) 0.03;

    private ContainerItem origcontainers[] = null;
    private KnapsackItem origitems[] = null;

    // when performing multiple Knapsack, this is the margin in which
    // a certain solution is considered as good and kept in memory!
    // it is expressed as a percent value!
    float margintoaccept = 0;

    // these informations that are kept in memory time by time
    ContainerItem nowcontainers[] = null;
    ContainerItem oldcontainers[] = null;
    ContainerItem container = null; // container considered
    KnapsackItem items[] = null; // items considered for the above container
    int knapiterations = 20000000; // number of default iterations to find solution
    float nowweight = -1;
    float nowgain = -1;
    // the following hashtable contains the container as the key,
    // and a singleSolution object as the value!
    multipleSolution nowmulsolution = new multipleSolution();
    multipleSolution oldmulsolution = new multipleSolution();
    multipleSolution bestmulsolution = null;
    singleSolution nowsolution = null; // the single solution considered now ...

    private int current = 0;
    private int tasklength = 0;
    private boolean finished = false;

    private int calculateFact(int i) {
        int ret = i;
        if (i <= 1)
            return ret;
        else
            return ret * calculateFact(i - 1);
    }

    // called before taskExecute to check if everything is ok before
    // executing the task!
    public boolean canExecute(String processId) {
        if (origcontainers != null && origitems != null) {
            tasklength = calculateFact(origcontainers.length);
            current = 0;
            return true;
        } else
            return false;
    }

    // called when the task is launched
    public boolean taskExecute(String processId) {
        finished = false;
        if (origcontainers != null && origitems != null) {
            tasklength = calculateFact(origcontainers.length);
            current = 0;
        } else
            return false;
        run();
        return true;
    }

    // called to know if the task has finished!
    public boolean taskDone() {
        return finished;
    }

    // called to stop task execution!
    public void taskStop() {
        finished = true;
    }

    public int getTaskLength() {
        return tasklength;
    }

    public int getCurrent() {
        return current;
    }

    // this could be a JComponent to be put in the progressMonitor object!
    public Object getMessage() {
        if (bestmulsolution != null) {
            return "Totally filled " + bestmulsolution.allfilled + " of " +
                    origcontainers.length + " dirs, unused space " +
                    ((int) bestmulsolution.unusedspace) + " MB ...";
        } else
            return "Totally filled 0 of " + origcontainers.length + " dirs ... ";
    }

    public void setContainers(ContainerItem cont[]) {
        origcontainers = cont;
    }

    public void setItems(KnapsackItem item[]) {
        origitems = item;
    }

    public Object[] getContainers() {
        return origcontainers;
    }

    public Object[] getItems() {
        return origitems;
    }

    public Object[] getItems(ContainerItem cont) {
        singleSolution sol = (singleSolution) bestmulsolution.get(cont);
        if (sol == null)
            return null;
        Object ret[] = new Object[sol.item.size()];
        for (int i = 0; i < ret.length; i++)
            ret[i] = ((Knapsacknode) sol.item.get(i)).thisitem;
        return ret;
    }

    public float getFilled(ContainerItem cont) {
        singleSolution sol = (singleSolution) bestmulsolution.get(cont);
        if (sol == null)
            return -1;
        else
            return sol.filled;
    }

    public float getUnused(ContainerItem cont) {
        singleSolution sol = (singleSolution) bestmulsolution.get(cont);
        if (sol == null)
            return -1;
        else
            return sol.remains;
    }

    public float getGain(ContainerItem cont) {
        singleSolution sol = (singleSolution) bestmulsolution.get(cont);
        if (sol == null)
            return -1;
        else
            return sol.gain;
    }

    public float getTotalUnused() {
        if (bestmulsolution != null)
            return bestmulsolution.unusedspace;
        else
            return -1;
    }

    public float getTotalGain() {
        if (bestmulsolution != null)
            return bestmulsolution.totalgain;
        else
            return -1;
    }

    public int getTotalInsertedItems() {
        if (bestmulsolution != null)
            return bestmulsolution.itemsinserted;
        else
            return -1;
    }

    private class Knapsacknode {
        int index = -1; // identifies the item in the original vector!
        int lastsonindex = -1; // identifies the index of the lastson that has been made!
        Knapsacknode father = null;
        Knapsacknode son = null;
        KnapsackItem thisitem = null;

        public void addsons() {
            lastsonindex = index + 1;
            for (int i = lastsonindex; i < items.length && knapiterations > 0 && !finished; i++) {
                if (nowweight + items[i].getWeight() < container.getCapacity() + 0.1) {
                    son = new Knapsacknode();
                    son.index = i;
                    son.father = this;
                    son.thisitem = items[i];
                    nowweight += items[i].getWeight();
                    nowgain += items[i].getGain();
                    if (container.getCapacity() - nowweight < 0.1) {
                        knapiterations = 0;
                        updateSolution(son);
                        // System.out.println("found maximum");
                    } else if (!finished)
                        son.addsons();
                } else {
                    // no more items can be contained, found a possible
                    // solution that can be inserted!
                    updateSolution(this);
                }
                knapiterations--;
            }
            if (knapiterations == 0)
                System.out.println("iteration finished");
            if (knapiterations > 0)
                updateSolution(this);
            nowweight -= thisitem.getWeight();
            nowgain -= thisitem.getGain();
        }
    }

    // all the informations about the solution of a single Knapsack
    private class singleSolution {
        float remains = 0; // remains to fill in this container
        float filled = 0;
        float gain = 0;
        ContainerItem container = null;
        ArrayList item = null; // list of the Knapsacknodes that have to be put in this container
    }

    private class multipleSolution {
        float unusedspace = -1;
        float totalgain = -1;
        int allfilled = 0;
        int itemsinserted = 0;
        private ArrayList containersfilled = new ArrayList();
        private Hashtable hash = new Hashtable();

        public void put(Object key, Object value) {
            singleSolution tmp = (singleSolution) value;
            if (tmp.item != null && tmp.item.size() > 0)
                containersfilled.add(key);
            tmp.remains = tmp.container.getCapacity() - tmp.filled;
            if (tmp.remains / tmp.container.getCapacity() < totallyfilled)
                allfilled++;
            if (debug > 0) {
                System.out.print("container filled to " + tmp.filled + " of " + tmp.container.getCapacity());
                System.out.print(", inserted items indexes: ");
                for (int j = 0; j < tmp.item.size(); j++) {
                    KnapsackItem node = ((Knapsacknode) tmp.item.get(j)).thisitem;
                    for (int m = 0; m < origitems.length; m++) {
                        if (node.equals(origitems[m]))
                            System.out.print("" + m);
                    }
                    System.out.print(" (" + node.getWeight() + "); ");
                }
                System.out.println(" allfilled " + allfilled);
            }
            hash.put(key, value);
        }

        public Object get(Object key) {
            return hash.get(key);
        }

        public void fixLastContainer() {
            float left = 0;
            float gain = 0;
            if (containersfilled.size() == 1) {
                singleSolution tmp = (singleSolution) get((ContainerItem) containersfilled.get(0));
                left += tmp.remains;
                gain += tmp.gain;
                itemsinserted += tmp.item.size();
            } else if (containersfilled.size() > 1) {
                singleSolution tmp = null;
                // update the left space and the number of inserted items
                for (int i = 0; i < containersfilled.size(); i++) {
                    tmp = (singleSolution) get((ContainerItem) containersfilled.get(i));
                    if (i < containersfilled.size() - 1)
                        left += tmp.remains;
                    gain += tmp.gain;
                    itemsinserted += tmp.item.size();
                }
                singleSolution last = (singleSolution) get(containersfilled.get(containersfilled.size() - 1));
                // if not all the items have been inserted, the last object has to be
                // considered as unused space!
                if (itemsinserted < origitems.length)
                    left += last.remains;
                float lastremains = last.remains;
                float weight = last.filled;
                for (int i = 0; i < nowcontainers.length; i++) {
                    tmp = (singleSolution) get(nowcontainers[i]);
                    if (tmp == null)
                        return;
                    if (tmp.item.size() == 0 && weight < tmp.container.getCapacity() + 0.01
                            && tmp.container.getCapacity() - weight < lastremains) {
                        tmp.item = last.item;
                        tmp.filled = weight;
                        tmp.gain = last.gain;
                        tmp.remains = tmp.container.getCapacity() - weight;
                        last.item = new ArrayList();
                        last.gain = 0;
                        last.filled = 0;
                        last.remains = 0;
                        last = tmp;
                    }
                }
            }
            totalgain = gain;
            unusedspace = left;
            if (debug > 0)
                System.out.println(
                        "Unused space " + unusedspace + " inserted " + itemsinserted + " of " + origitems.length);
            // now move the last items array in a container that can fill everything up!

        }
    }

    public void stop() {
        finished = true;
    }

    private void updateSolution(Knapsacknode lastnode) {
        // a possible solution is given ... this solution is compared
        // to the existing one for the same Knapsack considered, and the
        // solution is substituted if it is better than the old one!
        if (nowsolution == null) {
            nowsolution = new singleSolution();
            nowsolution.container = container;
            nowsolution.gain = nowgain;
            nowsolution.filled = nowweight;
            nowsolution.item = new ArrayList();
            while (lastnode.father != null) {
                nowsolution.item.add(lastnode);
                lastnode = lastnode.father;
            }
        }
        if (nowgain > nowsolution.gain) {
            nowsolution.gain = nowgain;
            nowsolution.filled = nowweight;
            nowsolution.item = new ArrayList();
            while (lastnode.father != null) {
                nowsolution.item.add(lastnode);
                lastnode = lastnode.father;
            }
        }
    }

    private void updateBestSolution() {
        if (bestmulsolution == null)
            bestmulsolution = nowmulsolution;
        else if (nowmulsolution.allfilled > bestmulsolution.allfilled)
            bestmulsolution = nowmulsolution;
        else if (nowmulsolution.allfilled >= bestmulsolution.allfilled) {
            if (nowmulsolution.unusedspace < bestmulsolution.unusedspace)
                bestmulsolution = nowmulsolution;
            else if (nowmulsolution.unusedspace == bestmulsolution.unusedspace) {
                if (nowmulsolution.itemsinserted > bestmulsolution.itemsinserted)
                    bestmulsolution = nowmulsolution;
            }
        }
    }

    // here there are some global variables and classes to scramble
    // the containers ... if necessary the original containers can
    // be reordered and only a part of them could be reordered!
    private class combinationScrambler {
        private int size = -1;
        private Hashtable inserted = null;
        private combinationNode root = null;
        private combinationNode lastleaf = null;

        private class combinationNode {
            int index = -1;
            combinationNode son = null, father = null;

            combinationNode(int ind) {
                index = ind;
            }

            public void remove() {
                if (father != null)
                    father.remove(index);
                else
                    index++;
            }

            private void remove(int ind) {
                inserted.remove(Integer.valueOf(ind));
                int i = ind + 1;
                while (i < size && (inserted.containsKey(Integer.valueOf(i)) || i == index || i == ind))
                    i++;
                if (i >= size) {
                    if (father != null)
                        father.remove(index);
                    else {
                        inserted.remove(Integer.valueOf(index));
                        index++;
                        inserted.put(Integer.valueOf(index), "");
                        if (index < size)
                            son = new combinationNode(0);
                        son.father = this;
                        son.add();
                    }
                } else {
                    son = new combinationNode(i);
                    son.father = this;
                    son.add();
                }
            }

            public void add() {
                inserted.put(Integer.valueOf(index), "");
                if (inserted.size() < size) {
                    int i = 0;
                    while ((inserted.containsKey(Integer.valueOf(i)) || i == index) && i < size)
                        i++;
                    if (i < size) {
                        son = new combinationNode(i);
                        son.father = this;
                        son.add();
                    }
                } else
                    lastleaf = this;
            }
        }

        int[] next() {
            if (inserted == null) {
                root = new combinationNode(0);
                inserted = new Hashtable();
                root.add();
            } else {
                lastleaf.remove();
            }
            if (root.index == size)
                return null;
            int res[] = new int[size];
            combinationNode tmp = root;
            for (int i = 0; i < size; i++) {
                res[i] = tmp.index;
                tmp = tmp.son;
            }
            return res;
        }

        combinationScrambler(int num) {
            size = num;
        }
    }

    private void run() {
        combinationScrambler comb = new combinationScrambler(origcontainers.length);

        if (origcontainers != null && origitems != null) {
            // the containers have to be permutated so that a better
            // solution can be found ... for example, if there are two
            // containers of 40 and 30 and an item of 25, it is better
            // to put it into the second one, so that it is almost filled!
            // So the subsequent algorithm must be repeated for every
            // permutation, and the solution has to be compared to the
            // previous one: the best will be choosen! The best is based
            // on the remaining space of the containers in which there is
            // at least one item!!

            while (!finished) {
                int combination[] = null;
                combination = comb.next();
                if (combination == null)
                    break;
                else {
                    if (debug > 0) {
                        for (int i = 0; i < combination.length; i++)
                            System.out.print(combination[i] + " ");
                        System.out.println();
                    }
                }
                nowcontainers = new ContainerItem[origcontainers.length];
                nowmulsolution = new multipleSolution();
                items = new KnapsackItem[origitems.length];
                for (int i = 0; i < items.length; i++)
                    items[i] = origitems[i];

                // change the nowcontainers vector basing upon the old
                // nowcontainers vector ... for example, if the old indexes
                // where 0,1,2,3 and the new are 0,1,3,2
                // the items that filled in the best way the containers 0 and 1
                // can be kept as good and copied into the new solution.
                // The cycle below will search for the optimum combination
                // starting from container number 3!
                for (int i = 0; i < combination.length; i++) {
                    nowcontainers[i] = origcontainers[combination[i]];
                    /*
                     * System.out.println("new "+nowcontainers[i]);
                     * if (oldcontainers!=null)
                     * System.out.println("old "+oldcontainers[i]);
                     */
                }

                for (int j = 0; j < nowcontainers.length && !finished; j++) {
                    container = nowcontainers[j];

                    if (false)// oldcontainers!=null && container.equals(oldcontainers[j]))
                    {
                        // put in the old solution
                        nowmulsolution.put(container, oldmulsolution.get(container));
                    } else {
                        nowweight = 0;
                        nowgain = 0;
                        Knapsacknode root = new Knapsacknode();
                        root.thisitem = origitems[0];
                        root.index = -1;
                        root.addsons();
                        nowmulsolution.put(container, nowsolution);
                    }
                    // the solution has been calculated, now remove
                    // the inserted elements from the item array!
                    ArrayList sol = ((singleSolution) nowmulsolution.get(container)).item;

                    if (sol != null) {
                        ArrayList tmp = new ArrayList();
                        Hashtable tmphash = new Hashtable();
                        for (int i = 0; i < sol.size(); i++)
                            tmphash.put(((Knapsacknode) sol.get(i)).thisitem, "");
                        for (int i = 0; i < items.length; i++)
                            if (!tmphash.containsKey(items[i]))
                                tmp.add(items[i]);
                        items = new KnapsackItem[tmp.size()];
                        for (int i = 0; i < items.length; i++)
                            items[i] = (KnapsackItem) tmp.get(i);
                    }
                    nowsolution = null;
                    knapiterations = 2000000;
                }
                nowmulsolution.fixLastContainer();
                updateBestSolution();

                oldcontainers = nowcontainers;
                oldmulsolution = nowmulsolution;
                current++;
                if (bestmulsolution.allfilled == origcontainers.length)
                    break;
            }
            // print the solution
            /*
             * for (int i=0;i<origcontainers.length;i++)
             * {
             * System.out.println("Container "+i+" capacity "+origcontainers[i].getCapacity(
             * ));
             * singleSolution sol=(singleSolution)bestmulsolution.get(origcontainers[i]);
             * System.out.println("Filled "+sol.filled);
             * ArrayList tmp=sol.item;
             * for (int j=0;tmp!=null && j<tmp.size();j++)
             * {
             * KnapsackItem node=((Knapsacknode)tmp.get(j)).thisitem;
             * System.out.print("item ");
             * System.out.print(""+getIndex(node));
             * System.out.println(" weight "+node.getWeight());
             * }
             * }
             * System.out.println("\nTotal unused space "+bestmulsolution.unusedspace+
             * " total gain "+bestmulsolution.totalgain);
             */
        }
    }

    int getIndex(KnapsackItem node) {
        for (int m = 0; m < origitems.length; m++)
            if (node.equals(origitems[m]))
                return m;
        return -1;
    }
}
