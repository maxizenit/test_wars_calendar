package org.itmo;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MagicCalendar {
    // Перечисление типов встреч
    public enum MeetingType {
        WORK,
        PERSONAL
    }

    private record Meeting(LocalTime time, MeetingType meetingType) {}

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final Map<String, List<Meeting>> meetings = new HashMap<>();

    /**
     * Запланировать встречу для пользователя.
     *
     * @param user имя пользователя
     * @param time временной слот (например, "10:00")
     * @param type тип встречи (WORK или PERSONAL)
     * @return true, если встреча успешно запланирована, false если:
     * - в этот временной слот уже есть встреча, и правило замены не выполняется,
     * - лимит в 5 встреч в день уже достигнут.
     */
    public boolean scheduleMeeting(String user, String time, MeetingType type) {
        try {
            LocalTime localTime = LocalTime.parse(time, TIME_FORMATTER);

            List<Meeting> userMeetings = getMeetingsByUser(user);
            if (userMeetings.size() >= 5) {
                return false;
            }

            List<Meeting> oldMeetings = userMeetings.stream()
                                                    .filter(m -> {
                                                        LocalTime start = localTime.minusHours(1);
                                                        LocalTime end = localTime.plusHours(1);

                                                        return m.time.isAfter(start) && m.time.isBefore(end);
                                                    })
                                                    .toList();
            if (!oldMeetings.isEmpty()) {
                if (oldMeetings.stream()
                               .anyMatch(m -> m.meetingType == MeetingType.PERSONAL)) {
                    return false;
                }
                userMeetings.removeAll(oldMeetings);
            }

            Meeting meeting = new Meeting(localTime, type);
            userMeetings.add(meeting);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Получить список всех встреч пользователя.
     *
     * @param user имя пользователя
     * @return список временных слотов, на которые запланированы встречи.
     */
    public List<String> getMeetings(String user) {
        return getMeetingsByUser(user).stream()
                                      .sorted(Comparator.comparing(o -> o.time))
                                      .map(m -> m.time.toString())
                                      .toList();
    }

    /**
     * Отменить встречу для пользователя по заданному времени.
     *
     * @param user имя пользователя
     * @param time временной слот, который нужно отменить.
     * @return true, если встреча была успешно отменена; false, если:
     * - встреча в указанное время отсутствует,
     * - встреча имеет тип PERSONAL (отменять можно только WORK встречу).
     */
    public boolean cancelMeeting(String user, String time) {
        try {
            LocalTime localTime = LocalTime.parse(time, TIME_FORMATTER);
            List<Meeting> userMeetings = getMeetingsByUser(user);
            Meeting meeting = userMeetings.stream()
                                          .filter(m -> m.time.equals(localTime))
                                          .findFirst()
                                          .orElseThrow();
            if (meeting.meetingType == MeetingType.WORK) {
                userMeetings.remove(meeting);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private List<Meeting> getMeetingsByUser(String user) {
        if (!meetings.containsKey(user)) {
            meetings.put(user, new ArrayList<>());
        }
        return meetings.get(user);
    }
}
