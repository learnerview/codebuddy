package org.codebuddy.core.services;

import org.codebuddy.core.models.Problem;
import org.codebuddy.core.models.Platform;
import org.codebuddy.core.models.Difficulty;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;

public class AnalyticsService {
    private final ProblemService problemService;

    public AnalyticsService() {
        this.problemService = new ProblemService();
    }

    public AnalyticsService(ProblemService problemService) {
        this.problemService = problemService;
    }

    public List<Problem> getAllProblems() throws Exception {
        return problemService.getAllProblems();
    }

    public Map<String, Long> getProblemsPerDay() throws Exception {
        List<Problem> problems = getAllProblems();
        return problems.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getSolvedDate().format(DateTimeFormatter.ISO_DATE),
                        TreeMap::new,
                        Collectors.counting()
                ));
    }

    public Map<String, Long> getProblemsByPlatform() throws Exception {
        List<Problem> problems = getAllProblems();
        return problems.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getPlatform().getDisplayName(),
                        Collectors.counting()
                ));
    }

    public Map<String, Long> getProblemsByDifficulty() throws Exception {
        List<Problem> problems = getAllProblems();
        return problems.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getDifficulty().getDisplayName(),
                        Collectors.counting()
                ));
    }

    public Map<String, Integer> getTimeSpentPerDay() throws Exception {
        List<Problem> problems = getAllProblems();
        return problems.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getSolvedDate().format(DateTimeFormatter.ISO_DATE),
                        TreeMap::new,
                        Collectors.summingInt(Problem::getTimeTakenMin)
                ));
    }

    public List<Problem> getRecentProblems(int n) throws Exception {
        List<Problem> problems = getAllProblems();
        return problems.stream()
                .sorted(Comparator.comparing(Problem::getSolvedDate).reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    public Map<String, Long> getProblemsPerMonth() throws Exception {
        List<Problem> problems = getAllProblems();
        return problems.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getSolvedDate().getYear() + "-" + String.format("%02d", p.getSolvedDate().getMonthValue()),
                        TreeMap::new,
                        Collectors.counting()
                ));
    }
    public Map<String, Long> getProblemsPerYear() throws Exception {
        List<Problem> problems = getAllProblems();
        return problems.stream()
                .collect(Collectors.groupingBy(
                        p -> String.valueOf(p.getSolvedDate().getYear()),
                        TreeMap::new,
                        Collectors.counting()
                ));
    }
    public Map<String, Long> getProblemsByTimeRange() throws Exception {
        List<Problem> problems = getAllProblems();
        int[] ranges = {0, 10, 20, 30, 60, 120, 9999};
        String[] labels = {"<10", "10-19", "20-29", "30-59", "60-119", "120+"};
        Map<String, Long> result = new LinkedHashMap<>();
        for (String label : labels) result.put(label, 0L);
        for (Problem p : problems) {
            int t = p.getTimeTakenMin();
            String label = t < 10 ? "<10" :
                          t < 20 ? "10-19" :
                          t < 30 ? "20-29" :
                          t < 60 ? "30-59" :
                          t < 120 ? "60-119" : "120+";
            result.put(label, result.get(label) + 1);
        }
        return result;
    }
    public Map<String, Long> getProblemsByDayOfWeek() throws Exception {
        List<Problem> problems = getAllProblems();
        return problems.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getSolvedDate().getDayOfWeek().toString(),
                        () -> new TreeMap<>(Comparator.comparingInt(day ->
                            List.of("MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY","SUNDAY").indexOf(day)
                        )),
                        Collectors.counting()
                ));
    }
    public Map<String, Double> getAvgTimeByPlatform() throws Exception {
        List<Problem> problems = getAllProblems();
        return problems.stream().collect(Collectors.groupingBy(
                p -> p.getPlatform().getDisplayName(),
                Collectors.averagingInt(Problem::getTimeTakenMin)
        ));
    }
    public Map<String, Double> getAvgTimeByDifficulty() throws Exception {
        List<Problem> problems = getAllProblems();
        return problems.stream().collect(Collectors.groupingBy(
                p -> p.getDifficulty().getDisplayName(),
                Collectors.averagingInt(Problem::getTimeTakenMin)
        ));
    }
    public Map<String, Integer> getStreaks() throws Exception {
        List<Problem> problems = getAllProblems();
        Set<String> days = problems.stream().map(p -> p.getSolvedDate().toLocalDate().toString()).collect(Collectors.toSet());
        List<String> sortedDays = new ArrayList<>(days);
        Collections.sort(sortedDays);
        int maxStreak = 0, currentStreak = 0;
        String prev = null;
        Map<String, Integer> streaks = new LinkedHashMap<>();
        for (String day : sortedDays) {
            if (prev == null || LocalDate.parse(day).minusDays(1).toString().equals(prev)) {
                currentStreak++;
            } else {
                currentStreak = 1;
            }
            streaks.put(day, currentStreak);
            maxStreak = Math.max(maxStreak, currentStreak);
            prev = day;
        }
        streaks.put("Max Streak", maxStreak);
        return streaks;
    }
    public Map<String, Map<String, Long>> getProblemsByPlatformPerMonth() throws Exception {
        List<Problem> problems = getAllProblems();
        Map<String, Map<String, Long>> result = new TreeMap<>();
        for (Problem p : problems) {
            String month = p.getSolvedDate().getYear() + "-" + String.format("%02d", p.getSolvedDate().getMonthValue());
            String platform = p.getPlatform().getDisplayName();
            result.computeIfAbsent(month, k -> new HashMap<>());
            result.get(month).put(platform, result.get(month).getOrDefault(platform, 0L) + 1);
        }
        return result;
    }
    public Map<String, Map<String, Long>> getProblemsByDifficultyPerMonth() throws Exception {
        List<Problem> problems = getAllProblems();
        Map<String, Map<String, Long>> result = new TreeMap<>();
        for (Problem p : problems) {
            String month = p.getSolvedDate().getYear() + "-" + String.format("%02d", p.getSolvedDate().getMonthValue());
            String difficulty = p.getDifficulty().getDisplayName();
            result.computeIfAbsent(month, k -> new HashMap<>());
            result.get(month).put(difficulty, result.get(month).getOrDefault(difficulty, 0L) + 1);
        }
        return result;
    }
} 