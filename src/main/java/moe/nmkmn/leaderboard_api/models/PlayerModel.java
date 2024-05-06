package moe.nmkmn.leaderboard_api.models;

public class PlayerModel {
    private final String UUID;
    private String lastName;
    private double balance;
    private long blockBreak;
    private long blockPlace;
    private long playTime;

    public PlayerModel(String UUID, String lastName, double balance, long blockBreak, long blockPlace, long playTime) {
        this.UUID = UUID;
        this.lastName = lastName;
        this.balance = balance;
        this.blockBreak = blockBreak;
        this.blockPlace = blockPlace;
        this.playTime = playTime;
    }

    public String getUUID() {
        return UUID;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public long getBlockBreak() {
        return blockBreak;
    }

    public void setBlockBreak(long blockBreak) {
        this.blockBreak = blockBreak;
    }

    public long getBlockPlace() {
        return blockPlace;
    }

    public void setBlockPlace(long blockPlace) {
        this.blockPlace = blockPlace;
    }

    public long getPlayTime() {
        return playTime;
    }

    public void setPlayTime(long playTime) {
        this.playTime = playTime;
    }
}
