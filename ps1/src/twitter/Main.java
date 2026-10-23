/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Main runs the social network analysis on a list of tweets and prints the
 * most-followed users.
 *
 * The original starter code downloads live tweets from an MIT web address, but
 * that address now returns an HTML page instead of JSON, so this version uses
 * a hardcoded sample list of tweets.
 *
 * Remember to run with assertions enabled (VM argument: -ea).
 */
public class Main {

    public static void main(String[] args) {
        // make sure assertions are enabled
        boolean assertionsEnabled = false;
        assert assertionsEnabled = true;
        if (!assertionsEnabled) {
            throw new Error("Always run main and tests with assertions enabled");
        }

        List<Tweet> tweets = sampleTweets();
        System.out.println("Read " + tweets.size() + " tweets");

        Timespan span = Extract.getTimespan(tweets);
        System.out.println("Tweets span from " + span.getStart() + " to " + span.getEnd());

        Set<String> mentioned = Extract.getMentionedUsers(tweets);
        System.out.println("Mentioned users: " + mentioned);

        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(tweets);
        System.out.println("Follows graph: " + followsGraph);

        List<String> influencers = SocialNetwork.influencers(followsGraph);
        System.out.println("Top 10 most-followed users:");
        for (int i = 0; i < Math.min(10, influencers.size()); i++) {
            System.out.println((i + 1) + ". " + influencers.get(i));
        }
    }

    private static List<Tweet> sampleTweets() {
        Instant t1 = Instant.parse("2016-02-17T10:00:00Z");
        Instant t2 = Instant.parse("2016-02-17T11:30:00Z");
        Instant t3 = Instant.parse("2016-02-17T13:15:00Z");

        return Arrays.asList(
                new Tweet(1, "alice", "hello @bob and @carol", t1),
                new Tweet(2, "dave", "great talk @bob", t1),
                new Tweet(3, "erin", "agree with @bob and @carol", t2),
                new Tweet(4, "frank", "thanks @carol", t2),
                new Tweet(5, "bob", "thanks everyone @alice", t3),
                new Tweet(6, "gina", "reading @bob and @alice", t3));
    }
}