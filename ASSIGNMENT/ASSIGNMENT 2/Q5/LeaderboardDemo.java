import java.util.*;

class Team {
    final String name;
    final int points;
    final int goalDiff;

    Team(String name, int points, int goalDiff) {
        this.name = name;
        this.points = points;
        this.goalDiff = goalDiff;
    }

    @Override
    public String toString() {
        return name + "(" + points + "," + goalDiff + ")";
    }
}

class Leaderboard<T> {
    private final List<T> items = new ArrayList<>();
    private final Comparator<? super T> order;

    Leaderboard(Comparator<? super T> order) {
        this.order = order;
    }

    void add(T item) {
        items.add(item);
    }

    List<T> ranking() {
        // TODO 1: Return a new sorted copy of items using order, without modifying items
        List<T> copy = new ArrayList<>(items);
        copy.sort(order);
        return Collections.unmodifiableList(copy);
    }
}

public class LeaderboardDemo {
    public static void main(String[] args) {
        // TODO 2: Build the comparator: points (high first), then goal difference (high first), then name (A->Z)
        Comparator<Team> byRank = Comparator
                .comparingInt((Team t) -> t.points).reversed()
                .thenComparingInt((Team t) -> t.goalDiff).reversed()
                .thenComparing(t -> t.name);

        Leaderboard<Team> board = new Leaderboard<>(byRank);
        board.add(new Team("Panthers", 9, 4));
        board.add(new Team("Eagles", 9, 6));
        board.add(new Team("Cobras", 6, 1));
        board.add(new Team("Bisons", 9, 4));
        System.out.println(board.ranking());

        // Faulty comparator demonstration (ignores tie-breakers)
        Comparator<Team> faultyByPointsOnly = Comparator.comparingInt((Team t) -> t.points).reversed();
        List<Team> faultyList = new ArrayList<>(List.of(
                new Team("Panthers", 9, 4),
                new Team("Eagles", 9, 6),
                new Team("Bisons", 9, 4)
        ));
        faultyList.sort(faultyByPointsOnly);
        System.out.println("Faulty comparator (points only): " + faultyList);
    }
}
