package com.up1234567.unistar.central.support.core.clust;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UnistarNodeVoter {

    private ConcurrentMap<String, Integer> votes = new ConcurrentHashMap<>();
    // 状态机
    // 当前状态 状态和事件Type保持一致
    @Setter
    @Getter
    private int current = IUnistarClustMsg.TYPE_VOTE_START;

    @Getter
    private String winner = null;

    public void joinVoter(String node) {
        votes.put(node, 0);
    }

    public void voteVoter(String node) {
        votes.put(node, votes.get(node) + 1);
    }

    /**
     * @return
     */
    public String chooseWinner() {
        int voteNumer = 0;

        for (Map.Entry<String, Integer> entry : votes.entrySet()) {
            if (entry.getValue() > voteNumer) {
                voteNumer = entry.getValue();
                winner = entry.getKey();
            }
        }
        return winner;
    }

    public boolean checkWinner(String voter) {
        return voter != null && voter.equals(winner);
    }
}
