package ticket.booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ticket.booking.entities.Train;
import ticket.booking.util.LocalDbPathUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TrainService {

    private List<Train> trainList;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final File trainDbFile;

    public TrainService() throws IOException {
        trainDbFile = LocalDbPathUtil.resolveDbFile("trains.json");
        trainList = objectMapper.readValue(trainDbFile, new TypeReference<List<Train>>() {});
    }

    public List<Train> searchTrains(String source, String destination) {
        return trainList.stream()
                .filter(train -> validTrain(train, source, destination))
                .collect(Collectors.toList());
    }

    public void addTrain(Train newTrain) {
        Optional<Train> existingTrain = trainList.stream()
                .filter(train -> train.getTrainId().equalsIgnoreCase(newTrain.getTrainId()))
                .findFirst();

        if (existingTrain.isPresent()) {
            updateTrain(newTrain);
        } else {
            trainList.add(newTrain);
            saveTrainListToFile();
        }
    }

    public void updateTrain(Train updatedTrain) {
        OptionalInt index = IntStream.range(0, trainList.size())
                .filter(i -> trainList.get(i).getTrainId().equalsIgnoreCase(updatedTrain.getTrainId()))
                .findFirst();

        if (index.isPresent()) {
            trainList.set(index.getAsInt(), updatedTrain);
            saveTrainListToFile();
        } else {
            addTrain(updatedTrain);
        }
    }

    public boolean releaseSeat(String trainId, int row, int seat) {
        Optional<Train> trainOpt = trainList.stream()
                .filter(train -> train.getTrainId().equalsIgnoreCase(trainId))
                .findFirst();

        if (trainOpt.isEmpty()) {
            return false;
        }

        Train train = trainOpt.get();
        List<List<Integer>> seats = train.getSeats();
        if (row < 0 || row >= seats.size() || seat < 0 || seat >= seats.get(row).size()) {
            return false;
        }

        seats.get(row).set(seat, 0);
        train.setSeats(seats);
        saveTrainListToFile();
        return true;
    }

    private void saveTrainListToFile() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(trainDbFile, trainList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean validTrain(Train train, String source, String destination) {
        List<String> stationOrder = train.getStations();
        if (stationOrder == null || stationOrder.isEmpty()) {
            return false;
        }

        List<String> normalizedStations = stationOrder.stream()
                .map(station -> station.toLowerCase().trim())
                .collect(Collectors.toList());

        int sourceIndex = normalizedStations.indexOf(source.toLowerCase().trim());
        int destinationIndex = normalizedStations.indexOf(destination.toLowerCase().trim());

        return sourceIndex != -1 && destinationIndex != -1 && sourceIndex < destinationIndex;
    }
}
