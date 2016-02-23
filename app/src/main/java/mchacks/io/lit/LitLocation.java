package mchacks.io.lit;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class LitLocation {

    public String address;
    public int votes;
    public ArrayList<Post> posts = new ArrayList<>();

    public LitLocation(String address, int votes, ArrayList<Post> posts) {
        this.address = address;
        this.votes = votes;
        this.posts = posts;
    }

}
