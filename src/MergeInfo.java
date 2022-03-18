public class MergeInfo {
    private int start;
    private int length;


    // These are in bytes
    /**
     * Basic constructor
     *
     * @param startPoint
     *            point to start for the sort
     * @param lengthOfRun
     *            length of the run being sorted
     */
    public MergeInfo(int startPoint, int lengthOfRun) {
        this.start = startPoint;
        this.length = lengthOfRun;
    }


    /**
     * basic getter
     *
     * @return the startPoint
     */
    public int getStart() {
        return this.start;
    }


    /**
     * basic getter
     *
     * @return the length
     */
    public int getLength() {
        return this.length;
    }


    /**
     * this is a helper for when I create the arraylist
     *
     * @return the total of length + start
     */
    public int getTotal() {
        return this.start + this.length;
    }


    /**
     * run to set
     *
     * @param startToSet
     *            is the start to set
     */
    public void setStart(int startToSet) {
        this.start = startToSet;
    }


    /**
     * length to set
     *
     * @param lengthToSet
     *            the int to set length to
     */
    public void setLength(int lengthToSet) {
        this.length = lengthToSet;
    }

    @Override
    public String toString() {
        return this.start + " " + this.length;
    }
}
