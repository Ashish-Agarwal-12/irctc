package ticket.booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ticket.booking.entities.Ticket;
import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.util.LocalDbPathUtil;
import ticket.booking.util.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserBookingService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<User> userList;
    private User user;
    private final File userDbFile;

    public UserBookingService(User user) throws IOException {
        this.user = user;
        this.userDbFile = LocalDbPathUtil.resolveDbFile("users.json");
        loadUserListFromFile();
    }

    public UserBookingService() throws IOException {
        this.userDbFile = LocalDbPathUtil.resolveDbFile("users.json");
        loadUserListFromFile();
    }

    private void loadUserListFromFile() throws IOException {
        userList = objectMapper.readValue(userDbFile, new TypeReference<List<User>>() {});
    }

    public Boolean loginUser() {
        Optional<User> foundUser = userList.stream().filter(user1 ->
                user1.getName().equalsIgnoreCase(user.getName())
                        && UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword())
        ).findFirst();

        if (foundUser.isPresent()) {
            User loggedIn = foundUser.get();
            // Keep the plain password from the login attempt for this session.
            loggedIn.setPassword(user.getPassword());
            this.user = loggedIn;
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public Boolean signUp(User user1) {
        try {
            boolean exists = userList.stream()
                    .anyMatch(existing -> existing.getName().equalsIgnoreCase(user1.getName()));
            if (exists) {
                return Boolean.FALSE;
            }
            userList.add(user1);
            saveUserListToFile();
            return Boolean.TRUE;
        } catch (IOException ex) {
            return Boolean.FALSE;
        }
    }

    private void saveUserListToFile() throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(userDbFile, userList);
    }

    public void fetchBookings() {
        if (user == null) {
            System.out.println("Please login first.");
            return;
        }
        user.printTickets();
    }

    public Boolean cancelBooking(String ticketId) {
        if (user == null || ticketId == null || ticketId.isEmpty()) {
            return false;
        }

        Optional<Ticket> ticketToCancel = user.getTicketsBooked().stream()
                .filter(ticket -> ticket.getTicketId().equals(ticketId))
                .findFirst();

        if (ticketToCancel.isEmpty()) {
            return false;
        }

        Ticket ticket = ticketToCancel.get();
        boolean removed = user.getTicketsBooked().removeIf(t -> t.getTicketId().equals(ticketId));
        if (!removed) {
            return false;
        }

        try {
            syncLoggedInUserToList();
            saveUserListToFile();

            if (ticket.getTrain() != null
                    && ticket.getSeatRow() != null
                    && ticket.getSeatCol() != null) {
                TrainService trainService = new TrainService();
                trainService.releaseSeat(ticket.getTrain().getTrainId(), ticket.getSeatRow(), ticket.getSeatCol());
            }
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    public List<Train> getTrains(String source, String destination) {
        try {
            TrainService trainService = new TrainService();
            return trainService.searchTrains(source, destination);
        } catch (IOException ex) {
            return new ArrayList<>();
        }
    }

    public List<List<Integer>> fetchSeats(Train train) {
        return train.getSeats();
    }

    public Boolean bookTrainSeat(Train train, int row, int seat, String source, String destination, String dateOfTravel) {
        if (user == null) {
            return Boolean.FALSE;
        }

        try {
            TrainService trainService = new TrainService();
            List<List<Integer>> seats = train.getSeats();
            if (row >= 0 && row < seats.size() && seat >= 0 && seat < seats.get(row).size()) {
                if (seats.get(row).get(seat) == 0) {
                    seats.get(row).set(seat, 1);
                    train.setSeats(seats);
                    trainService.addTrain(train);

                    Ticket ticket = new Ticket(
                            UUID.randomUUID().toString(),
                            user.getUserId(),
                            source.toLowerCase().trim(),
                            destination.toLowerCase().trim(),
                            dateOfTravel,
                            train,
                            row,
                            seat
                    );
                    user.getTicketsBooked().add(ticket);
                    syncLoggedInUserToList();
                    saveUserListToFile();
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (IOException ex) {
            return Boolean.FALSE;
        }
    }

    public User getUser() {
        return user;
    }

    private void syncLoggedInUserToList() {
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getUserId().equals(user.getUserId())) {
                userList.set(i, user);
                return;
            }
        }
    }
}
