package store;

public class ZSetEntry implements Comparable<ZSetEntry>{
    public double score;
    public String member;

    public ZSetEntry(double score , String member){
        this.score = score;
        this.member = member;
    }

    @Override
    public int compareTo(ZSetEntry other) {

       int scoreComp = Double.compare(this.score , other.score);
       if(scoreComp != 0) return scoreComp;

       return this.member.compareTo(other.member);
    }
}
