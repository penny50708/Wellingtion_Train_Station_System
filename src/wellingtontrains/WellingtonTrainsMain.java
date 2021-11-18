package wellingtontrains;

import ecs100.UI;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

class WellingtonTrainsMain {
	HashMap<String, Station> listStations = new HashMap<>();
	HashMap<String, TrainLine> trainLines = new HashMap<>();

	public WellingtonTrainsMain() {
		UI.initialise();
		UI.addButton("All Stations(A-Z)", this::printStation);
		UI.addButton("All Train lines(A-Z)", this::listAllLine);
		UI.addTextField("<html>List Lines through A Station<br>Please enter the station name:<br>e.g.Wellington</html>",
				this::listLineOfStation);
		UI.addTextField(
				"<html>List Stations along A Train Line:<br>Please enter a train line name:<br>e.g.Wellington_Melling</html>",
				this::listStationThroughLine);
		UI.addButton("Find Your Train Lines", this::printLineName);
		UI.addButton("Find Your Next Train Service ", this::findNextServiceTime);
		UI.addButton("Find Your Best Trip", this::findATrip);
		UI.addButton("Geographic Map ", this::addGeographicMap);
		UI.addButton("System Map ", this::addsystemMap);
		UI.addButton("Clear", this::remove);
		UI.addButton("Quit", UI::quit);

		loadStation();
		loadTrainLines();

	}

	public void loadStation() {

		try {
			Scanner scan = new Scanner(new File("stations.data"));
			while (scan.hasNext()) {
				String name = scan.next();
				int zone = scan.nextInt();
				double dist = scan.nextDouble();
				Station station = new Station(name, zone, dist);
				listStations.put(name, station);

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void printStation() {
		UI.clearPanes();

		Set set = listStations.keySet();
		Object[] arr = set.toArray();
		Arrays.sort(arr);
		for (Object key : arr) {
			UI.println(key + ":" + "\t\t" + listStations.get(key));
			UI.println();
			UI.drawImage("system-map.png", 0, 0, 500, 620);
		}

	}

	public void loadTrainLines() {

		try {
			Scanner scan = new Scanner(new File("train-lines.data"));
			while (scan.hasNext()) {
				String name = scan.next();
				TrainLine tl = new TrainLine(name);
				addStationToTrainLine(tl);
				addServicesToTheLine(tl);
				trainLines.put(name, tl);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void listAllLine() {
		UI.clearPanes();
		Set set = trainLines.keySet();
		Object[] arr = set.toArray();
		Arrays.sort(arr);
		for (Object key : arr) {
			UI.println(key + ":" + "\t\t" + trainLines.get(key));
			UI.println();
			UI.drawImage("system-map.png", 0, 0, 500, 620);
		}
	}

	public void addStationToTrainLine(TrainLine line) {
		String fileName = line.getName() + "-stations.data";
		try {
			Scanner scan = new Scanner(new File(fileName));
			while (scan.hasNext()) {
				Station st = listStations.get(scan.nextLine());
				line.addStation(st);
				st.addTrainLine(line);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void listLineOfStation(String name) {
		UI.clearPanes();
		this.listStations.containsKey(name);

		Station st = listStations.get(name);

		if (st != null) {
			for (TrainLine tl : st.getTrainLines()) {
				UI.println(tl.toString());
				UI.println();
			}
		} else {
			UI.println("Invalid station");
		}
	}

	public void listStationThroughLine(String line) {
		UI.clearPanes();
		this.trainLines.containsKey(line);
		TrainLine tl = trainLines.get(line);
		if (tl != null) {
			for (Station st : tl.getStations()) {
				UI.println(st.toString());
				UI.println();
			}
		} else {
			UI.println("Invalid name");
		}

	}

	public void printLineName() {
		UI.clearPanes();
		String s1 = UI.askString("Please enter the Starting Station:\nCapitalize each word");
		String s2 = UI.askString("Please enter the Destination Station:\nCapitalize each word");
		UI.println();
		if (s1 != null && s2 != null) {

			ArrayList<TrainLine> tl = getTrainLine(s1, s2);
			if (tl.size() > 0) {
				for (TrainLine tls : tl) {
					UI.println("Train line between two stations:" + tls.getName());
					UI.println();
				}
			} else {
				UI.println("There's no line between two stations or\nPlease check if each word is capitalized");
			}
		} else {
			UI.println("You have to fill both starting station and destination statoin.");

		}

	}

	private ArrayList<TrainLine> getTrainLine(String s1, String s2) {

		ArrayList<TrainLine> trainLines = new ArrayList<>();
		Station st1 = listStations.get(s1);

		if (st1 != null) {
			for (TrainLine tl : st1.getTrainLines()) {
				ArrayList<Station> stations = new ArrayList<>(tl.getStations());
				int i = stations.indexOf(st1);
				for (int j = i; j < stations.size(); j++) {
					if (stations.get(j).getName().equalsIgnoreCase(s2)) {
						trainLines.add(tl);
					}
				}
			}
		}
		return trainLines;
	}

	public void addServicesToTheLine(TrainLine line) {

		try {
			String fileName = line.getName() + "-services.data";
			Scanner scan = new Scanner(new File(fileName));
			while (scan.hasNext()) {
				Scanner scan1 = new Scanner(scan.nextLine());
				TrainService ser = new TrainService(line);
				boolean isFirstStop = true;
				while (scan1.hasNext()) {
					ser.addTime(scan1.nextInt(), isFirstStop);
				}

				line.addTrainService(ser);

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void findNextServiceTime() {
		UI.clearPanes();
		String stationName = UI.askString("Enter station:e.g.Wellington");
		int timeEntered = UI.askInt("Enter time:e.g.600 or1245");
		UI.println();
		Station st = listStations.get(stationName);

		if (st != null) {
			for (TrainLine line : st.getTrainLines()) {
				int index = line.getStations().indexOf(st);
				TrainService ts = findNextServiceTool(line, timeEntered, index);
				if (ts != null) {
					UI.println(ts.toString());
					UI.println();
				}
			}
		} else {
			UI.println("Sorry there is no next train service or\nPlease check if each word is capitalized");
		}

	}

	private TrainService findNextServiceTool(TrainLine tl, int time, int stationIndex) {
		for (TrainService ts : tl.getTrainServices()) {
			int currentTime = ts.getTimes().get(stationIndex);
			if (currentTime >= time) {
				UI.println("Next depature time:" + currentTime);
				return ts;
			}
		}

		return null;
	}

	public void findATrip() {
		UI.clearPanes();
		String startStationName = UI.askString("Enter starting station:e.g.Wellington");
		String destinationStationName = UI.askString("Enter destination station:e.g.Wellington");
		int timeEntered = UI.askInt("Enter time:e.g.600 or1245");
		Station sst = listStations.get(startStationName);
		Station dst = listStations.get(destinationStationName);
		UI.println();
		ArrayList<TrainLine> lines = getTrainLine(startStationName, destinationStationName);
		if (sst != null && dst != null) {

			for (TrainLine line : lines) {
				UI.println("Line:" + line.getName());

				int index = line.getStations().indexOf(listStations.get(startStationName));

				TrainService ts = findNextServiceTool(line, timeEntered, index);

				if (ts != null) {
					UI.println(ts.toString());
					int desIndex = line.getStations().indexOf(listStations.get(destinationStationName));

					for (int i = 0; i < ts.getTimes().size(); i++) {
						if (i == desIndex)
							UI.println("Arrive Time: " + ts.getTimes().get(desIndex));

					}
				}
				UI.println();
			}
		} else {
			UI.println("Sorry there is not available trip or\nPlease check if each word is capitalized");
		}

	}

	private void addGeographicMap() {
		UI.clearPanes();
		UI.drawImage("geographic-map.png", 0, 0);
	}

	private void addsystemMap() {
		UI.clearPanes();
		UI.drawImage("system-map.png", 0, 0, 500, 620);

	}

	private void remove() {
		UI.clearPanes();
	}

	public static void main(String[] args) {
		new WellingtonTrainsMain();
	}
}
