package ticket.booking;

import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.services.UserBookingService;
import ticket.booking.util.UserServiceUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class App {

    public static void main(String[] args) {
        System.out.println("Welcome to IRCTC Ticket Booking");
        Scanner scanner = new Scanner(System.in);
        UserBookingService userBookingService;

        try {
            userBookingService = new UserBookingService();
        } catch (IOException ex) {
            System.out.println("Unable to load user database. Exiting.");
            ex.printStackTrace();
            return;
        }

        while (true) {
            System.out.println();
            System.out.println("1. Sign up");
            System.out.println("2. Login");
            System.out.println("3. Fetch Bookings");
            System.out.println("4. Search Trains");
            System.out.println("5. Book a Seat");
            System.out.println("6. Cancel my Booking");
            System.out.println("7. Exit the App");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> signUp(scanner, userBookingService);
                case "2" -> userBookingService = login(scanner);
                case "3" -> fetchBookings(userBookingService);
                case "4" -> searchTrains(scanner, userBookingService);
                case "5" -> bookSeat(scanner, userBookingService);
                case "6" -> cancelBooking(scanner, userBookingService);
                case "7" -> {
                    System.out.println("Thank you for using IRCTC Ticket Booking. Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void signUp(Scanner scanner, UserBookingService userBookingService) {
        System.out.print("Enter username: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        if (name.isEmpty() || password.isEmpty()) {
            System.out.println("Username and password cannot be empty.");
            return;
        }

        User user = new User(
                name,
                password,
                UserServiceUtil.hashPassword(password),
                new ArrayList<>(),
                UUID.randomUUID().toString()
        );

        Boolean signedUp = userBookingService.signUp(user);
        if (Boolean.TRUE.equals(signedUp)) {
            System.out.println("Sign up successful! Please login.");
        } else {
            System.out.println("Sign up failed. Username may already exist.");
        }
    }

    private static UserBookingService login(Scanner scanner) {
        System.out.print("Enter username: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User user = new User(name, password, null, new ArrayList<>(), null);
        try {
            UserBookingService userBookingService = new UserBookingService(user);
            if (Boolean.TRUE.equals(userBookingService.loginUser())) {
                System.out.println("Login successful. Welcome, " + userBookingService.getUser().getName() + "!");
                return userBookingService;
            }
            System.out.println("Login failed. Invalid username or password.");
            return new UserBookingService();
        } catch (IOException ex) {
            System.out.println("Unable to login due to a database error.");
            try {
                return new UserBookingService();
            } catch (IOException nested) {
                throw new RuntimeException(nested);
            }
        }
    }

    private static void fetchBookings(UserBookingService userBookingService) {
        if (!isLoggedIn(userBookingService)) {
            return;
        }
        System.out.println("Your bookings:");
        userBookingService.fetchBookings();
    }

    private static void searchTrains(Scanner scanner, UserBookingService userBookingService) {
        System.out.print("Enter source station: ");
        String source = scanner.nextLine().trim();
        System.out.print("Enter destination station: ");
        String destination = scanner.nextLine().trim();

        List<Train> trains = userBookingService.getTrains(source, destination);
        if (trains.isEmpty()) {
            System.out.println("No trains found for " + source + " -> " + destination);
            return;
        }

        System.out.println("Available trains:");
        for (int i = 0; i < trains.size(); i++) {
            Train train = trains.get(i);
            System.out.println((i + 1) + ". " + train.getTrainInfo());
            System.out.println("   Stations: " + train.getStations());
            System.out.println("   Times: " + train.getStationTimes());
        }
    }

    private static void bookSeat(Scanner scanner, UserBookingService userBookingService) {
        if (!isLoggedIn(userBookingService)) {
            return;
        }

        System.out.print("Enter source station: ");
        String source = scanner.nextLine().trim();
        System.out.print("Enter destination station: ");
        String destination = scanner.nextLine().trim();
        System.out.print("Enter date of travel (yyyy-mm-dd): ");
        String dateOfTravel = scanner.nextLine().trim();

        List<Train> trains = userBookingService.getTrains(source, destination);
        if (trains.isEmpty()) {
            System.out.println("No trains found for " + source + " -> " + destination);
            return;
        }

        System.out.println("Available trains:");
        for (int i = 0; i < trains.size(); i++) {
            System.out.println((i + 1) + ". " + trains.get(i).getTrainInfo());
        }

        System.out.print("Select a train by number: ");
        int trainIndex = readInt(scanner) - 1;
        if (trainIndex < 0 || trainIndex >= trains.size()) {
            System.out.println("Invalid train selection.");
            return;
        }

        Train selectedTrain = trains.get(trainIndex);
        List<List<Integer>> seats = userBookingService.fetchSeats(selectedTrain);

        System.out.println("Seat map (0 = available, 1 = booked):");
        for (int i = 0; i < seats.size(); i++) {
            System.out.println("Row " + i + ": " + seats.get(i));
        }

        System.out.print("Enter row number: ");
        int row = readInt(scanner);
        System.out.print("Enter seat number (column): ");
        int seat = readInt(scanner);

        Boolean booked = userBookingService.bookTrainSeat(
                selectedTrain, row, seat, source, destination, dateOfTravel
        );

        if (Boolean.TRUE.equals(booked)) {
            System.out.println("Seat booked successfully!");
            System.out.println("Your bookings:");
            userBookingService.fetchBookings();
        } else {
            System.out.println("Unable to book seat. It may already be taken or the selection is invalid.");
        }
    }

    private static void cancelBooking(Scanner scanner, UserBookingService userBookingService) {
        if (!isLoggedIn(userBookingService)) {
            return;
        }

        System.out.println("Your bookings:");
        userBookingService.fetchBookings();
        System.out.print("Enter ticket id to cancel: ");
        String ticketId = scanner.nextLine().trim();

        Boolean cancelled = userBookingService.cancelBooking(ticketId);
        if (Boolean.TRUE.equals(cancelled)) {
            System.out.println("Booking cancelled successfully.");
        } else {
            System.out.println("Unable to cancel booking. Check the ticket id and try again.");
        }
    }

    private static boolean isLoggedIn(UserBookingService userBookingService) {
        if (userBookingService.getUser() == null) {
            System.out.println("Please login first.");
            return false;
        }
        return true;
    }

    private static int readInt(Scanner scanner) {
        while (true) {
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException ex) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }
}
