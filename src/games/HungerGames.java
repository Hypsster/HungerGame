package games;

import java.util.ArrayList;

/**
 * This class contains methods to represent the Hunger Games using BSTs.
 * Moves people from input files to districts, eliminates people from the game,
 * and determines a possible winner.
 *
 * @author Pranay Roni
 * @author Maksims Kurjanovics Kravcenko
 * @author Kal Pandit
 */
public class HungerGames {

    private ArrayList<District> districts;  // all districts in Panem.
    private TreeNode            game;       // root of the BST. The BST contains districts that are still in the game.

    /**
     * ***** DO NOT REMOVE OR UPDATE this method *********
     * Default constructor, initializes a list of districts.
     */
    public HungerGames() {
        districts = new ArrayList<>();
        game = null;
        StdRandom.setSeed(2023);
    }

    /**
     * ***** DO NOT REMOVE OR UPDATE this method *********
     * Sets up Panem, the universe in which the Hunger Games takes place.
     * Reads districts and people from the input file.
     *
     * @param filename will be provided by client to read from using StdIn
     */
    public void setupPanem(String filename) {
        StdIn.setFile(filename);  // open the file - happens only once here
        setupDistricts(filename);
        setupPeople(filename);
    }

    /**
     * Reads the following from input file:
     * - Number of districts
     * - District ID's (insert in order of insertion)
     * Insert districts into the districts ArrayList in order of appearance.
     *
     * @param filename will be provided by client to read from using StdIn
     */
    public void setupDistricts (String filename) {
        int numDistricts = StdIn.readInt();

        // loops through each district and adds it to the distracts array
        for (int i = 0; i < numDistricts; i++) {
            int districtId = StdIn.readInt();
            District newDistrict = new District(districtId); // Assuming District constructor takes districtId as a parameter
            districts.add(newDistrict);
        }
    }

    /**
     * Reads the following from input file (continues to read from the SAME input file as setupDistricts()):
     * Number of people
     * Space-separated: first name, last name, birth month (1-12), age, district id, effectiveness
     * Districts will be initialized to the instance variable districts
     *
     * Persons will be added to corresponding district in districts defined by districtID
     *
     * @param filename will be provided by client to read from using StdIn
     */
    public void setupPeople (String filename) {
        int numPeople = StdIn.readInt();

        // o(n * m) ? for the nested loop, could possibly be o(1) if a hashmap was used instead

        // loops through each persons data in the input file.
        for (int i = 0; i < numPeople; i++) {
            String firstName = StdIn.readString();
            String lastName = StdIn.readString();
            int birthMonth = StdIn.readInt();
            int age = StdIn.readInt();
            int districtId = StdIn.readInt();
            int effectiveness = StdIn.readInt();

            // creates a person
            Person newPerson = new Person(birthMonth, firstName, lastName, age, districtId, effectiveness);

            // checks eligibility and set
            if (age >= 12 && age < 18) {
                newPerson.setTessera(true);
            }

            // inserts the person into the appropriate district and population list
            for (District district : districts) {
                if (district.getDistrictID() == districtId) {
                    if (birthMonth % 2 == 0) {
                        district.addEvenPerson(newPerson);
                    } else {
                        district.addOddPerson(newPerson);
                    }
                    break;
                }
            }
        }
    }

    /**
     * Adds a district to the game BST.
     * If the district is already added, do nothing
     *
     * @param root        the TreeNode root which we access all the added districts
     * @param newDistrict the district we wish to add
     */
    public void addDistrictToGame(TreeNode root, District newDistrict) {
        // Base case: If the root is null, create a new TreeNode and set it as the root
        if (root == null) {
            root = new TreeNode(newDistrict, null, null);
            game = root;  // Update the game root if necessary
            districts.remove(newDistrict); // Remove the district from the ArrayList
            return;
        }
        
        if (newDistrict.getDistrictID() < root.getDistrict().getDistrictID()) {
            // If the new district's ID is smaller, go to the left subtree
            if (root.getLeft() == null) {
                root.setLeft(new TreeNode(newDistrict, null, null)); // Insert the new district here
                districts.remove(newDistrict); // Remove the district from the ArrayList
            } else {
                addDistrictToGame(root.getLeft(), newDistrict); // Recur into the left subtree
            }
        } else if (newDistrict.getDistrictID() > root.getDistrict().getDistrictID()) {
            // If the new district's ID is greater, go to the right subtree
            if (root.getRight() == null) {
                root.setRight(new TreeNode(newDistrict, null, null)); // Insert the new district here
                districts.remove(newDistrict); // Remove the district from the ArrayList
            } else {
                addDistrictToGame(root.getRight(), newDistrict); // Recur into the right subtree
            }
        }
        // If the new district's ID is equal to the root's district ID, do nothing (IDs are unique)
    }
    /**
     * Searches for a district inside of the BST given the district id.
     *
     * @param id the district to search
     * @return the district if found, null if not found
     */
    public District findDistrict(int id) {
        // Start at the root of the BST
        TreeNode current = game;

        if (current == null) {
            System.out.println("The tree is empty.");
        }

        while (current != null) {
            System.out.println("Checking district with ID: " + current.getDistrict().getDistrictID());

            if (current.getDistrict().getDistrictID() == id) {
                System.out.println("Found the district with ID: " + id);
                return current.getDistrict();
            } else if (id < current.getDistrict().getDistrictID()) {
                current = current.getLeft();
            } else {
                current = current.getRight();
            }
        }

        System.out.println("District not found.");
        return null;
    }

    /**
     * Selects two duelers from the tree, following these rules:
     * - One odd person and one even person should be in the pair.
     * - Dueler with Tessera (age 12-18, use tessera instance variable) must be
     * retrieved first.
     * - Find the first odd person and even person (separately) with Tessera if they
     * exist.
     * - If you can't find a person, use StdRandom.uniform(x) where x is the respective
     * population size to obtain a dueler.
     * - Add odd person dueler to person1 of new DuelerPair and even person dueler to
     * person2.
     * - People from the same district cannot fight against each other.
     *
     * @return the pair of dueler retrieved from this method.
     */

    public DuelPair selectDuelers() {
        // find a person with tessera from the odd population
        Person person1 = findPerson(true, true, -1);

        // find a person with tessera from the even population, not from the same district as person1
        Person person2 = findPerson(false, true, person1 != null ? person1.getDistrictID() : -1);

        // If a person with tessera from the odd population not found, find any person from the odd population
        if (person1 == null) {
            person1 = findPerson(true, false, person2 != null ? person2.getDistrictID() : -1);
        }

        // If a person with tessera from the even population not found, find any person from the even population
        if (person2 == null) {
            person2 = findPerson(false, false, person1 != null ? person1.getDistrictID() : -1);
        }
        if (person1 != null && person2 != null) {
            removePersonFromDistrict(person1);
            removePersonFromDistrict(person2);
            return new DuelPair(person1, person2);
        }
        return null;
    }

    private Person findPerson(boolean isOdd, boolean withTessera, int excludeDistrictId) {
        return findPersonRecursive(getRoot(), isOdd, withTessera, excludeDistrictId);
    }

    private Person findPersonRecursive(TreeNode node, boolean isOdd, boolean withTessera, int excludeDistrictId) {
        if (node == null) {
            return null;
        }

        District district = node.getDistrict();
        ArrayList<Person> population = isOdd ? district.getOddPopulation() : district.getEvenPopulation();

        // Skip if the same district as the other dueler
        if (district.getDistrictID() != excludeDistrictId) {
            for (Person person : population) {
                if (withTessera && person.getTessera()) {
                    return person;
                }
            }

            if (!withTessera && !population.isEmpty()) {
                int randomIndex = StdRandom.uniform(population.size());
                return population.get(randomIndex);
            }
        }

        //  search in the left and right subtrees
        Person leftResult = findPersonRecursive(node.getLeft(), isOdd, withTessera, excludeDistrictId);
        if (leftResult != null) {
            return leftResult;
        }

        return findPersonRecursive(node.getRight(), isOdd, withTessera, excludeDistrictId);
    }

    private void removePersonFromDistrict(Person person) {
        District district = findDistrict(person.getDistrictID());
        ArrayList<Person> population = (person.getBirthMonth() % 2 == 0) ? district.getEvenPopulation() : district.getOddPopulation();
        population.remove(person);
    }


    /**
     * Deletes a district from the BST when they are eliminated from the game.
     * Districts are identified by id's.
     * If district does not exist, do nothing.
     *
     * This is similar to the BST delete we have seen in class.
     *
     * @param id the ID of the district to eliminate
     */

    public void eliminateDistrict(int id) {
        TreeNode parent = null;
        TreeNode current = game;  // 'game' is the root of the BST
        boolean isLeftChild = false;

        // Search for the node and its parent
        while (current != null && current.getDistrict().getDistrictID() != id) {
            parent = current;
            if (id < current.getDistrict().getDistrictID()) {
                isLeftChild = true;
                current = current.getLeft();
            } else {
                isLeftChild = false;
                current = current.getRight();
            }
        }

        // If the node wasn't found
        if (current == null) {
            return;
        }

        //  Node has no children
        if (current.getLeft() == null && current.getRight() == null) {
            if (parent == null) {
                game = null;
            } else if (isLeftChild) {
                parent.setLeft(null);
            } else {
                parent.setRight(null);
            }
        }
        // Node has one child
        else if (current.getLeft() == null) {
            if (parent == null) {
                game = current.getRight();
            } else if (isLeftChild) {
                parent.setLeft(current.getRight());
            } else {
                parent.setRight(current.getRight());
            }
        } else if (current.getRight() == null) {
            if (parent == null) {
                game = current.getLeft();
            } else if (isLeftChild) {
                parent.setLeft(current.getLeft());
            } else {
                parent.setRight(current.getLeft());
            }
        }
        // Node has two children
        else {
            TreeNode successorParent = current;
            TreeNode successor = current.getRight();
            while (successor.getLeft() != null) {
                successorParent = successor;
                successor = successor.getLeft();
            }

            if (successorParent != current) {
                successorParent.setLeft(successor.getRight());
            } else {
                successorParent.setRight(successor.getRight());
            }

            current.setDistrict(successor.getDistrict());

            if (parent == null) {
                game = current;
            } else if (isLeftChild) {
                parent.setLeft(current);
            } else {
                parent.setRight(current);
            }
        }
    }



    /**
     * Eliminates a dueler from a pair of duelers.
     * - Both duelers in the DuelPair argument given will duel
     * - Winner gets returned to their District
     * - Eliminate a District if it only contains a odd person population or even
     * person population
     *
     * @param pair of persons to fight each other.
     */
    public void eliminateDueler(DuelPair pair) {
        Person person1 = pair.getPerson1();
        Person person2 = pair.getPerson2();

        // If the pair is incomplete
        if (person1 == null || person2 == null) {
            Person existingPerson = (person1 != null) ? person1 : person2;
            District district = findDistrict(existingPerson.getDistrictID());
            if (existingPerson.getBirthMonth() % 2 == 0) {
                district.getEvenPopulation().add(existingPerson);
            } else {
                district.getOddPopulation().add(existingPerson);
            }
            return;
        }

        // If the pair is complete
        Person winner = person1.duel(person2);
        Person loser = (winner == person1) ? person2 : person1;

        // Return the winner back to their district
        District winnerDistrict = findDistrict(winner.getDistrictID());
        if (winner.getBirthMonth() % 2 == 0) {
            winnerDistrict.getEvenPopulation().add(winner);
        } else {
            winnerDistrict.getOddPopulation().add(winner);
        }

        // Check if the loser's district population reaches zero
        District loserDistrict = findDistrict(loser.getDistrictID());
        if (loserDistrict.getOddPopulation().isEmpty() || loserDistrict.getEvenPopulation().isEmpty()) {
            eliminateDistrict(loserDistrict.getDistrictID());
        }

        // Check if the winner's district population reaches zero
        if (winnerDistrict.getOddPopulation().isEmpty() || winnerDistrict.getEvenPopulation().isEmpty()) {
            eliminateDistrict(winnerDistrict.getDistrictID());
        }
    }






    /**
     * ***** DO NOT REMOVE OR UPDATE this method *********
     *
     * Obtains the list of districts for the Driver.
     *
     * @return the ArrayList of districts for selection
     */
    public ArrayList<District> getDistricts() {
        return this.districts;
    }

    /**
     * ***** DO NOT REMOVE OR UPDATE this method *********
     *
     * Returns the root of the BST
     */
    public TreeNode getRoot() {
        return game;
    }
}
