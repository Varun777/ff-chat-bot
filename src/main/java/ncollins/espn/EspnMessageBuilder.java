package ncollins.espn;

import ncollins.model.Order;
import ncollins.model.espn.*;
import java.util.List;

public class EspnMessageBuilder {
    private Espn espn = new Espn();
    private static final String NOPE = "i can't do that yet.";

    /***
     *  Builds message that displays the best/worst scores by a team all-time
     */
    public String buildScoresMessage(Order order, int total, boolean includePlayoffs){
        return buildScoresMessage(order, total, null, includePlayoffs);
    }

    /***
     *  Builds message that displays the best/worst scores by a team
     */
    public String buildScoresMessage(Order order, int total, Integer seasonId, boolean includePlayoffs){
        List<Score> scores = espn.getScoresSorted(order, total, seasonId, includePlayoffs);

        StringBuilder sb = new StringBuilder();
        sb.append(order.equals(Order.ASC) ? "Bottom " : "Top ").append(total + " Scores:\\n");

        for(int i=0; i < scores.size(); i++){
            Member member = espn.getMemberByTeamId(scores.get(i).getTeamId(), scores.get(i).getSeasonId());
            String memberName = member.getFirtName() + " " + member.getLastName();
            sb.append(i+1 + ": " + scores.get(i).getPoints() + " - " + memberName + " (" + scores.get(i).getMatchupPeriodId() + "/" + scores.get(i).getSeasonId() + ")\\n");
        }

        return sb.toString();
    }

    /***
     *  Builds message that displays the most/least points by a fantasy player in a week all-time
     */
    public String buildPlayersMessage(Order order, int total, Position position){
        return buildPlayersMessage(order, total, null, position);
    }

    /***
     *  Builds message that displays the most/least points by a fantasy player in a week
     */
    public String buildPlayersMessage(Order order, int total, Integer seasonId, Position position){
        return NOPE;
    }

    /***
     *  Builds message that displays the longest winning/losing streaks all-time
     */
    public String buildOutcomeStreakMessage(Outcome outcome, int total){
        return buildOutcomeStreakMessage(outcome, null, total);
    }

    /***
     *  Builds message that displays the longest winning/losing streaks
     */
    public String buildOutcomeStreakMessage(Outcome outcome, Integer seasonId, int total){
        return NOPE;
    }

    /***
     *  Builds message that displays the matchups for the current week
     */
    public String buildMatchupsMessage(){
        return NOPE;
    }

    /***
     *  Builds message that displays current standings
     */
    public String buildStandingsMessage(){
        List<Team> teams = espn.getTeamsSorted(Order.DESC, null, espn.getCurrentSeasonId());

        StringBuilder sb = new StringBuilder();
        sb.append("Standings:\\n");
        for(Team team : teams){
            sb.append(espn.getTeamAbbrev(team.getId(), team.getSeasonId()) + " ")
                    .append(team.getRecord().getOverall().getWins() + "-" + team.getRecord().getOverall().getLosses() + " ")
                    .append(String.format("%.1f", team.getRecord().getOverall().getPercentage()) + " ")
                    .append(String.format("%.1f", team.getRecord().getOverall().getPointsFor()) + " ")
                    .append(String.format("%.1f", team.getRecord().getOverall().getPointsAgainst()) + "\\n");
        }

        return sb.toString();
    }

    /***
     *  Builds message that displays best/worst records all-time
     */
    public String buildRecordsMessage(Order order, int total){
        return buildRecordsMessage(order, total, null);
    }

    /***
     *  Builds message that displays best/worst records in given year
     */
    public String buildRecordsMessage(Order order, int total, Integer seasonId){
        List<Team> teams = espn.getTeamsSorted(order, total, seasonId);

        StringBuilder sb = new StringBuilder();
        sb.append(order.equals(Order.ASC) ? "Bottom " : "Top ").append(total + " Records:\\n");
        for(int i=0; i < teams.size(); i++){
            Member member = espn.getMemberByTeamId(teams.get(i).getId(), teams.get(i).getSeasonId());
            String memberName = member.getFirtName() + " " + member.getLastName();

            sb.append(i+1 + ": " + memberName + " ")
              .append(teams.get(i).getRecord().getOverall().getWins() + "-" + teams.get(i).getRecord().getOverall().getLosses() + " ")
              .append(String.format("%.1f", teams.get(i).getRecord().getOverall().getPercentage()) + " ")
              .append(String.format("%.1f", teams.get(i).getRecord().getOverall().getPointsFor()) + " ")
              .append(String.format("%.1f", teams.get(i).getRecord().getOverall().getPointsAgainst()) + " ")
              .append("(" + teams.get(i).getSeasonId() + ")\\n");
        }

        return sb.toString();
    }

    /***
     *  Builds message that displays all jujus of all-time.
     *  Definition of a Juju: TODO
     */
    public String buildJujusMessage(){
        return buildJujusMessage(null);
    }

    /***
     *  Builds message that displays all jujus.
     *  Definition of a Juju: TODO
     */
    public String buildJujusMessage(Integer seasonId){
        return NOPE;
    }

    /***
     *  Builds message that displays all salties of all-time.
     *  Definition of a Salty: TODO
     */
    public String buildSaltiesMessage(){
        return buildSaltiesMessage(null);
    }

    /***
     *  Builds message that displays all salties.
     *  Definition of a Salty: TODO
     */
    public String buildSaltiesMessage(Integer seasonId){
        return NOPE;
    }

    /***
     *  Builds message that displays the biggest blowout matchups of all-time.
     */
    public String buildBlowoutsMessage(int total){
        return buildBlowoutsMessage(total, null);
    }

    /***
     *  Builds message that displays the biggest blowout matchups.
     */
    public String buildBlowoutsMessage(int total, Integer seasonId){
        List<Matchup> matchups = espn.getMatchupsSorted(Order.DESC, total, seasonId, false, false);

        StringBuilder sb = new StringBuilder();
            sb.append("Biggest Blowouts:\\n");
            for(int i=0; i < matchups.size(); i++){
                ScheduleItem.Residence winner = matchups.get(i).getScheduleItem().getWinner().equals("HOME") ? matchups.get(i).getScheduleItem().getHome() : matchups.get(i).getScheduleItem().getAway();
                ScheduleItem.Residence loser = matchups.get(i).getScheduleItem().getWinner().equals("AWAY") ? matchups.get(i).getScheduleItem().getHome() : matchups.get(i).getScheduleItem().getAway();

                Member winnerMember = espn.getMemberByTeamId(winner.getTeamId(), matchups.get(i).getSeasonId());
                String winnerName = winnerMember.getFirtName() + " " + winnerMember.getLastName();

                Member loserMember = espn.getMemberByTeamId(loser.getTeamId(), matchups.get(i).getSeasonId());
                String loserName = loserMember.getFirtName() + " " + loserMember.getLastName();

                sb.append(i+1 + ": " + String.format("%.2f", Math.abs(matchups.get(i).getScheduleItem().getHome().getTotalPoints() - matchups.get(i).getScheduleItem().getAway().getTotalPoints())) + ": ")
                  .append(winnerName + " " + winner.getTotalPoints() + " - ")
                  .append(loser.getTotalPoints() + " " + loserName + " ")
                  .append("(" + matchups.get(i).getScheduleItem().getMatchupPeriodId() + "/")
                  .append(matchups.get(i).getSeasonId() + ")\\n");
            }

        return sb.toString();
    }

    /***
     *  Builds message that displays the closest matchups of all-time.
     */
    public String buildHeartbreaksMessage(int total){
        return buildHeartbreaksMessage(total, null);
    }

    /***
     *  Builds message that displays the closest matchups.
     */
    public String buildHeartbreaksMessage(int total, Integer seasonId){
        List<Matchup> matchups = espn.getMatchupsSorted(Order.ASC, total, seasonId, false, false);

        StringBuilder sb = new StringBuilder();
        sb.append("Biggest Heartbreaks:\\n");
        for(int i=0; i < matchups.size(); i++){
            ScheduleItem.Residence winner = matchups.get(i).getScheduleItem().getWinner().equals("HOME") ? matchups.get(i).getScheduleItem().getHome() : matchups.get(i).getScheduleItem().getAway();
            ScheduleItem.Residence loser = matchups.get(i).getScheduleItem().getWinner().equals("AWAY") ? matchups.get(i).getScheduleItem().getHome() : matchups.get(i).getScheduleItem().getAway();

            Member winnerMember = espn.getMemberByTeamId(winner.getTeamId(), matchups.get(i).getSeasonId());
            String winnerName = winnerMember.getFirtName() + " " + winnerMember.getLastName();

            Member loserMember = espn.getMemberByTeamId(loser.getTeamId(), matchups.get(i).getSeasonId());
            String loserName = loserMember.getFirtName() + " " + loserMember.getLastName();

            sb.append(i+1 + ": " + String.format("%.2f", Math.abs(matchups.get(i).getScheduleItem().getHome().getTotalPoints() - matchups.get(i).getScheduleItem().getAway().getTotalPoints())) + ": ")
                    .append(loserName + " " + loser.getTotalPoints() + " - ")
                    .append(winner.getTotalPoints() + " " + winnerName + " ")
                    .append("(" + matchups.get(i).getScheduleItem().getMatchupPeriodId() + "/")
                    .append(matchups.get(i).getSeasonId() + ")\\n");
        }

        return sb.toString();
    }
}
