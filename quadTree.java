import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

public class quadTree{
    static ConcurrentHashMap <Integer,pointOfInterest> poiObjects = new ConcurrentHashMap<Integer,pointOfInterest>();
    static Set<quadTreeNode> leafNodes = new HashSet<quadTreeNode>();
    static int alpha = 3;
    static void loadPOI()throws FileNotFoundException, IOException{
        int poi_id;
        String s_number, x_value, y_value;
        double x_coordinate, y_coordinate;
        File poi_file = new File("poi.txt");
        Scanner dataScanner = new Scanner(poi_file);
        dataScanner.useDelimiter("\\n");
        
        try (BufferedReader br = new BufferedReader(new FileReader(poi_file))) 
        {
            String dataLine;
            while ((dataLine = br.readLine()) != null) 
            {
                StringTokenizer st1 = new StringTokenizer(dataLine);

                s_number = st1.nextToken();
                x_value = st1.nextToken();
                y_value = st1.nextToken();

                poi_id = Integer.parseInt(s_number);
                x_coordinate = Double.parseDouble(x_value); 
                y_coordinate = Double.parseDouble(y_value); 
        
                pointOfInterest poiData = new pointOfInterest(poi_id, x_coordinate, y_coordinate);
                poiObjects.put(poi_id, poiData);

            }    
        }
        dataScanner.close();
    }

    static region getRegion(region parentRegion, point splitPoint, String currentNodeType){
        //  Returns the region to which the POI belongs to
        region childRegion = new region();
        switch(currentNodeType){
            case "NW":
            childRegion.x_min = parentRegion.x_min;
            childRegion.y_min = splitPoint.y_coordinate;
            childRegion.x_max = splitPoint.x_coordinate;
            childRegion.y_max = parentRegion.y_max;
            break;
    
            case "SW":
            childRegion.x_min = parentRegion.x_min;
            childRegion.y_min = parentRegion.y_min;
            childRegion.x_max = splitPoint.x_coordinate;
            childRegion.y_max = splitPoint.y_coordinate;
            break;
    
            case "NE":
            childRegion.x_min = splitPoint.x_coordinate;
            childRegion.y_min = splitPoint.y_coordinate;
            childRegion.x_max = parentRegion.x_max;
            childRegion.y_max = parentRegion.y_max;
            break;
    
            case "SE":
            childRegion.x_min = splitPoint.x_coordinate;
            childRegion.y_min = parentRegion.y_min;
            childRegion.x_max = parentRegion.x_max;
            childRegion.y_max = splitPoint.y_coordinate;
            break;
        }
        return childRegion;
    }
    
    static quadTreeNode makeLeafNode(quadTreeNode parentNode, region parentRegion, point splitPoint, String currentNodeType){
        quadTreeNode childNode  = new quadTreeNode();
        childNode.leafNodeFlag = 1;
        childNode.bucketElementCount = 0;
        childNode.currentNodeType = currentNodeType;
        childNode.parentNode = parentNode;
    
        region currentRegion = getRegion(parentRegion, splitPoint, currentNodeType);
        childNode.x_splitpoint = (currentRegion.x_max + currentRegion.x_min) / 2;
        childNode.y_splitpoint = (currentRegion.y_max + currentRegion.y_min) / 2;
        childNode.file_name = childNode.toString();
        leafNodes.add(childNode);
        return childNode;
    }

    static void makeQuadTree(rootNode poiTree){
        //rootNode poiTree = new rootNode();
        region mbrRegion = new region();
        poiTree.x_min = 10;
        poiTree.x_max = 14;
        poiTree.y_min = 11;
        poiTree.y_max = 16;
        poiTree.x_splitpoint = (poiTree.x_min + poiTree.x_max) / 2;
        poiTree.y_splitpoint = (poiTree.y_min + poiTree.y_max) / 2;
        point currentSplitPoint = new point(poiTree.x_splitpoint, poiTree.y_splitpoint);
        mbrRegion.x_min = poiTree.x_min;
        mbrRegion.x_max = poiTree.x_max;
        mbrRegion.y_min = poiTree.y_min;
        mbrRegion.y_max = poiTree.y_max;
        poiTree.northwestChild = makeLeafNode(null, mbrRegion, currentSplitPoint, "NW");
        poiTree.northeastChild = makeLeafNode(null, mbrRegion, currentSplitPoint, "NE");
        poiTree.southwestChild = makeLeafNode(null, mbrRegion, currentSplitPoint, "SW");
        poiTree.southeastChild = makeLeafNode(null, mbrRegion, currentSplitPoint, "SE");
    }
    
    static boolean addPOI(rootNode treeName, pointOfInterest data, int queryType) throws IOException{
        //  Define the region
        region currentRegion = new region();
        currentRegion.x_max = treeName.x_max;
        currentRegion.x_min = treeName.x_min;
        currentRegion.y_max = treeName.y_max;
        currentRegion.y_min = treeName.y_min;
    
        //  Get the split point here
        point currentSplitPoint = new point((treeName.x_max + treeName.x_min) / 2, (treeName.y_max + treeName.y_min) / 2);
        currentSplitPoint.x_coordinate = (treeName.x_max + treeName.x_min) / 2;
        currentSplitPoint.y_coordinate = (treeName.y_max + treeName.y_min) / 2;
    
        if(insertData(treeName.northwestChild, data, currentRegion, currentSplitPoint, queryType)) return true;
        if(insertData(treeName.northeastChild, data, currentRegion, currentSplitPoint, queryType)) return true;
        if(insertData(treeName.southwestChild, data, currentRegion, currentSplitPoint, queryType)) return true;
        if(insertData(treeName.southeastChild, data, currentRegion, currentSplitPoint, queryType)) return true;
        return false;
    }

    static boolean insertData(quadTreeNode node, pointOfInterest data, region parentRegion, point splitPoint, int queryType) throws IOException
    {
        String currentNodeType = node.currentNodeType;
        region currentRegion = getRegion(parentRegion, splitPoint, currentNodeType);
        point currentSplitPoint = new point((currentRegion.x_max + currentRegion.x_min) / 2, (currentRegion.y_max + currentRegion.y_min) / 2);
        if(!chlildInRegion(currentRegion, data)){
            return false;
        }
        if(queryType == 0){    
            if(node.leafNodeFlag == 1){
                node.dataList.add(data);
                node.bucketElementCount = node.bucketElementCount + 1;
                if (node.bucketElementCount > alpha){
                    node.leafNodeFlag = 0;
                    node.northwestChild = makeLeafNode(node, currentRegion, currentSplitPoint, "NW");
                    node.northeastChild = makeLeafNode(node, currentRegion, currentSplitPoint, "NE");
                    node.southeastChild = makeLeafNode(node, currentRegion, currentSplitPoint, "SE");
                    node.southwestChild = makeLeafNode(node, currentRegion, currentSplitPoint, "SW");

                    // for eachdata point in node.list Insertdata to node
                    for( pointOfInterest dataPoint : node.dataList){
                        if(insertData(node.northwestChild, dataPoint, currentRegion, currentSplitPoint, queryType)) continue;
                        if(insertData(node.northeastChild, dataPoint, currentRegion, currentSplitPoint, queryType)) continue;
                        if(insertData(node.southwestChild, dataPoint, currentRegion, currentSplitPoint, queryType)) continue;
                        if(insertData(node.southeastChild, dataPoint, currentRegion, currentSplitPoint, queryType)) continue;
                    }
                    node.bucketElementCount = 0;
                    node.dataList = null;
                    leafNodes.remove(node);
                }
                return true;
            }
            else{
                if(insertData(node.northwestChild, data, currentRegion, currentSplitPoint, queryType)) return true;
                if(insertData(node.northeastChild, data, currentRegion, currentSplitPoint, queryType)) return true;
                if(insertData(node.southwestChild, data, currentRegion, currentSplitPoint, queryType)) return true;
                if(insertData(node.southeastChild, data, currentRegion, currentSplitPoint, queryType)) return true;
                return false;
            }
        }
        else if(queryType == 1){
            // Insertion after system is live. Insertion to the file with overflow
            if(node.leafNodeFlag == 1){
                int overflowBucketNumber = node.bucketElementCount / alpha; 
                String fileName = "";
                if (overflowBucketNumber > 0){
                    fileName = "poiDB/" + node.file_name + "_" + String.valueOf(overflowBucketNumber) + ".txt";
                }
                else{
                    fileName = "poiDB/" + node.file_name + ".txt";
                }
                File nodefile = new File(fileName);
                String lineToWrite = "";
                try (FileWriter fileWriter = new FileWriter(nodefile,true)) {
                    lineToWrite = String.valueOf(data.poiID) + " " + String.valueOf(data.x_coordinate) + " " + String.valueOf(data.y_coordinate)  + "\n";
                    fileWriter.write(lineToWrite);
                    fileWriter.close();
                    
                }
                node.bucketElementCount = node.bucketElementCount + 1;
                return true;
            }
            else{
                if(insertData(node.northwestChild, data, currentRegion, currentSplitPoint, queryType)) return true;
                if(insertData(node.northeastChild, data, currentRegion, currentSplitPoint, queryType)) return true;
                if(insertData(node.southwestChild, data, currentRegion, currentSplitPoint, queryType)) return true;
                if(insertData(node.southeastChild, data, currentRegion, currentSplitPoint, queryType)) return true;
                return false;
            }
            
        }
        return false;
    }

    static boolean regionIntersect(region regionOne, region regionTwo){
        // regionOne is the region of the child, regionTwo if the range given 
        if (regionOne.x_min <= regionTwo.x_max && regionOne.x_max >= regionTwo.x_min &&
        regionOne.y_min <= regionTwo.y_max && regionOne.y_max >= regionTwo.y_min){
            return true;
        }
        else{
            return false;
        }
    }

    static ArrayList<pointOfInterest> getPOI(rootNode treeName, region inputRegion)throws FileNotFoundException, IOException{
        ArrayList<pointOfInterest> dataList = new ArrayList<pointOfInterest>();
        region currentRegion = new region();
        currentRegion.x_max = treeName.x_max;
        currentRegion.x_min = treeName.x_min;
        currentRegion.y_max = treeName.y_max;
        currentRegion.y_min = treeName.y_min;
    
        point currentSplitPoint = new point((treeName.x_max + treeName.x_min) / 2, (treeName.y_max + treeName.y_min) / 2);
        if(!regionIntersect(currentRegion, inputRegion)){
            // Return empty arraylist
            return dataList;
        }
        else{
            //  Branch out find the POIS from the child nodes
            dataList.addAll(minePOI(treeName.northwestChild, currentRegion, currentSplitPoint, inputRegion));
            dataList.addAll(minePOI(treeName.southwestChild, currentRegion, currentSplitPoint, inputRegion));
            dataList.addAll(minePOI(treeName.northeastChild, currentRegion, currentSplitPoint, inputRegion));
            dataList.addAll(minePOI(treeName.southeastChild, currentRegion, currentSplitPoint, inputRegion));
            return dataList;
        }
        
    }
    
    static ArrayList<pointOfInterest> minePOI(quadTreeNode node, region parentRegion, point splitPoint, region inputRegion)throws FileNotFoundException, IOException{
        String currentNodeType  = node.currentNodeType;
        ArrayList<pointOfInterest> dataList = new ArrayList<pointOfInterest>();
        point currentSplitPoint = new point(node.x_splitpoint, node.y_splitpoint);
        region currentRegion = getRegion(parentRegion, splitPoint, currentNodeType);
        if (!regionIntersect(currentRegion, inputRegion)){
            return dataList;
        }
    
        if(node.leafNodeFlag == 1){
            // if the node is leaf node then get all the pois from the list and return it
            int overflowBucketNumber  = node.bucketElementCount / alpha;
            //System.out.println(overflowBucketNumber);
            //System.out.println(node.bucketElementCount);
            ArrayList<String> readList = new ArrayList<String>();

            //  Read data from files to readList
            String file_name = "poiDB/" + node.file_name +".txt";
            readList.addAll(readDatafromFile(file_name, null,1));
            for (int i = 1; i < overflowBucketNumber; i++){
                file_name = "poiDB/" + node.file_name + "_" + String.valueOf(i) + ".txt";
                readList.addAll(readDatafromFile(file_name, null,1));
            }

            // Convert to POI objects
            for (String each_line : readList){
                StringTokenizer st2 = new StringTokenizer(each_line);
                int s_number = Integer.parseInt(st2.nextToken());
                double x_coordinate = Double.parseDouble(st2.nextToken());
                double y_coordinate = Double.parseDouble(st2.nextToken());


                pointOfInterest tempPOI = new pointOfInterest(s_number, x_coordinate, y_coordinate);
                if (chlildInRegion(inputRegion, tempPOI)){
                    dataList.add(tempPOI);
                }
            }
            //for(pointOfInterest dataPoint : node.dataList){
            //    //System.out.println("Read " + dataPoint.poiID);
            //    if (chlildInRegion(inputRegion, dataPoint)){
            //        dataList.add(dataPoint);
            //    }
            //}
            return dataList;
        }
        else{
            dataList.addAll(minePOI(node.northwestChild, currentRegion, currentSplitPoint, inputRegion));
            dataList.addAll(minePOI(node.northeastChild, currentRegion, currentSplitPoint, inputRegion));
            dataList.addAll(minePOI(node.southwestChild, currentRegion, currentSplitPoint, inputRegion));
            dataList.addAll(minePOI(node.southeastChild, currentRegion, currentSplitPoint, inputRegion));
            return dataList;
        }
    
    }

    static boolean chlildInRegion(region region,  pointOfInterest data){
        if(region.x_min <= data.x_coordinate && data.x_coordinate <= region.x_max && region.y_min <= data.y_coordinate && data.y_coordinate <= region.y_max)
        {
            return true;
        }
        else{
            return false;
        }
    }

    static boolean deletePOI(rootNode treeName, pointOfInterest data) throws FileNotFoundException, IOException{
        //  Define the region
        region currentRegion = new region();
        currentRegion.x_max = treeName.x_max;
        currentRegion.x_min = treeName.x_min;
        currentRegion.y_max = treeName.y_max;
        currentRegion.y_min = treeName.y_min;
    
        //  Get the split point here
        point currentSplitPoint = new point((treeName.x_max + treeName.x_min) / 2, (treeName.y_max + treeName.y_min) / 2);
    
        if(removeData(treeName.northwestChild, data, currentRegion, currentSplitPoint)) return true;
        if(removeData(treeName.northeastChild, data, currentRegion, currentSplitPoint)) return true;
        if(removeData(treeName.southwestChild, data, currentRegion, currentSplitPoint)) return true;
        if(removeData(treeName.southeastChild, data, currentRegion, currentSplitPoint)) return true;
        return false;
    }


    static boolean removeData(quadTreeNode node, pointOfInterest data, region parentRegion, point splitPoint) throws FileNotFoundException, IOException
    {
        String currentNodeType = node.currentNodeType;
        region currentRegion = getRegion(parentRegion, splitPoint, currentNodeType);
        point currentSplitPoint = new point((currentRegion.x_max + currentRegion.x_min) / 2, (currentRegion.y_max + currentRegion.y_min) / 2);
        
        if(!chlildInRegion(currentRegion, data)){
            return false;
        }

        if(node.leafNodeFlag == 1){
            int overflowBucketNumber  = node.bucketElementCount / alpha;
            ArrayList<String> readList = new ArrayList<String>();

            //  Read data from files to readList
            String file_name = "poiDB/" + node.file_name +".txt";
            readList.addAll(readDatafromFile(file_name, data,0));
            for (int i = 1; i < overflowBucketNumber; i++){
                file_name = "poiDB/" + node.file_name + "_" + String.valueOf(i) + ".txt";
                readList.addAll(readDatafromFile(file_name, data,0));
            }
            node.bucketElementCount = 0;
            writeDatatoFile(readList, node);
            return true;
        }
        else{
                if(removeData(node.northwestChild, data, currentRegion, currentSplitPoint)) return true;
                if(removeData(node.northeastChild, data, currentRegion, currentSplitPoint)) return true;
                if(removeData(node.southwestChild, data, currentRegion, currentSplitPoint)) return true;
                if(removeData(node.southeastChild, data, currentRegion, currentSplitPoint)) return true;
                return false;
        }
    }

    static ArrayList<String> readDatafromFile(String file_name, pointOfInterest data, int queryType) throws FileNotFoundException, IOException{
        ArrayList<String> lineList = new ArrayList<String>();
        File filetoRead = new File(file_name);
        Scanner dataScanner = new Scanner(filetoRead);
        dataScanner.useDelimiter("\\n");
        //System.out.println("Here");
        try (BufferedReader br = new BufferedReader(new FileReader(filetoRead))) 
        {
            String dataLine;
            while ((dataLine = br.readLine()) != null)
            {
                StringTokenizer st1 = new StringTokenizer(dataLine);
                String s_number = st1.nextToken();
                if(queryType == 0){
                    if (!(data.poiID == Integer.valueOf(s_number))){
                        lineList.add(dataLine);
                    }
                }
                else if (queryType == 1){
                    lineList.add(dataLine);
                }
            }
        }
        if(queryType == 0){
            filetoRead.delete();
        }
        dataScanner.close();  
        return lineList;
    }

    static void writeDatatoFile(ArrayList<String> writeList, quadTreeNode node) throws IOException{
        int countinFile = 0;
        int overflowBucketNumber = 0;
        String file_name = "poiDB/" + node.file_name + ".txt";
        for(String line : writeList){
            FileWriter fileWriter = new FileWriter(file_name,true);
            fileWriter.write(line);
            fileWriter.write("\n");
            node.bucketElementCount = node.bucketElementCount + 1;
            countinFile = countinFile + 1;
            if (countinFile == alpha){
                overflowBucketNumber = overflowBucketNumber + 1;
                countinFile = 0;
                file_name = "poiDB/" + node.file_name + "_" + String.valueOf(overflowBucketNumber) + ".txt";
            }
            fileWriter.close();
        }
        
    }

    static void writeLeavestoFile(quadTreeNode node) throws IOException{
        File leafnodefile = new File("poiDB/" + node.file_name + ".txt");
        try (FileWriter fileWriter = new FileWriter(leafnodefile)) {
            String lineToWrite = "";
            for (pointOfInterest datapoint : node.dataList){
                lineToWrite = String.valueOf(datapoint.poiID) + " " + String.valueOf(datapoint.x_coordinate) + " " + String.valueOf(datapoint.y_coordinate)  + "\n";
                fileWriter.write(lineToWrite);
            }
            fileWriter.close(); 
        }
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        loadPOI();
        rootNode poiTree = new rootNode();
        makeQuadTree(poiTree);
        for (int i = 0; i < poiObjects.size(); i++){
            addPOI(poiTree,poiObjects.get(i),0);
        }
        //addPOI(poiTree,poiObjects.get(1));
        //System.out.println("Complete loading");
        //System.out.println(poiTree.southwestChild.dataList);
        //System.out.println(poiTree.northwestChild.file_name);
        //System.out.println(poiTree.southwestChild.file_name);
        region testregion = new region();
        //
        testregion.x_min = 12;
        testregion.y_min = 14;
        testregion.x_max = 13;
        testregion.y_max = 15;
        //
        ArrayList<pointOfInterest> data = new ArrayList<pointOfInterest>();
        data = getPOI(poiTree, testregion);
        ////System.out.println(poiObjects.size());
        for (pointOfInterest poi : data){
            System.out.println(poi.poiID);
        }
//
        for(quadTreeNode node : leafNodes){
            writeLeavestoFile(node);
        }
        //
        ////for (int i = 20; i < 98; i++){
        ////    addPOI(poiTree,poiObjects.get(i),1);
        ////}
        //deletePOI(poiTree, poiObjects.get(24));
        ////System.out.println(test);
        ////if (chlildInRegion(testregion, poiObjects.get(16))){
        //    System.out.println("Yes");
        //}
    } 
}

// To store the point of interest
class pointOfInterest{
    int poiID;
    double x_coordinate;
    double y_coordinate;
    public pointOfInterest(int poiID, double x_coordinate, double y_coordinate){
        this.poiID = poiID;
        this.x_coordinate = x_coordinate;
        this.y_coordinate = y_coordinate;
    }
}

//  To store the region of a node / input range
class region{
    double x_max;
    double x_min;
    double y_max;
    double y_min;
}

// To structure of the root node
class rootNode{
    double x_splitpoint;
    double y_splitpoint;
    double x_min;
    double x_max;
    double y_min;
    double y_max;
    quadTreeNode northwestChild;
    quadTreeNode southwestChild;
    quadTreeNode northeastChild;
    quadTreeNode southeastChild;
}

// To store all non-root node
class quadTreeNode{
    double x_splitpoint;
    double y_splitpoint;
    quadTreeNode northwestChild = null;
    quadTreeNode southwestChild = null;
    quadTreeNode northeastChild = null;
    quadTreeNode southeastChild = null;
    quadTreeNode parentNode = null;
    String currentNodeType = null;
    int bucketElementCount = 0;
    int leafNodeFlag = 0;
    String file_name = null;
    ArrayList<pointOfInterest> dataList = new ArrayList<pointOfInterest>();
}

class point{
    double x_coordinate;
    double y_coordinate;
    public point(double x_coordinate,double y_coordinate){
        this.x_coordinate = x_coordinate;
        this.y_coordinate = y_coordinate;
    }
}