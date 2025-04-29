

setImageType('FLUORESCENCE')
clearAnnotations()
clearDetections()

createAnnotationsFromPixelClassifier("ROI", 0.0, 0.0, "INCLUDE_IGNORED")

// StarDist section
import qupath.ext.stardist.StarDist2D
import qupath.lib.scripting.QP

selectAnnotations()
def modelPath = "/Users/chia-chianhsieh/Desktop/StarDist Models/dsb2018_heavy_augment.pb"

def stardist = StarDist2D
    .builder(modelPath)
    .channels('Blue')
    .normalizePercentiles(1, 99)
    .threshold(0.6)
    .pixelSize(0.1804)
    .cellExpansion(5.0)
    .measureShape()
    .measureIntensity()
    .build()

def pathObjects = QP.getSelectedObjects()
def imageData = QP.getCurrentImageData()

if (pathObjects.isEmpty()) {
    QP.getLogger().error("No parent objects are selected!")
    return
}

stardist.detectObjects(imageData, pathObjects)
stardist.close()
println('StarDist done!')

// Subcellular detection

runPlugin('qupath.imagej.detect.cells.SubcellularDetection', '{"detection[Channel 1]":60.0,"detection[Channel 2]":60.0,"detection[Channel 3]":-1.0,"doSmoothing":false,"splitByIntensity":true,"splitByShape":true,"spotSizeMicrons":0.5,"minSpotSizeMicrons":0.0,"maxSpotSizeMicrons":2.0,"includeClusters":false}')

println "Total detections: " + getDetectionObjects().size()


// --- Parameters ---

double maxDistMicrons = 0.5
def cal = getCurrentImageData().getServer().getPixelCalibration()
double pixelSize = cal.getAveragedPixelSizeMicrons()
double maxDistPixels = maxDistMicrons / pixelSize

// --- Collect red and green spots by their displayed name ---
def red = []
def green = []

getDetectionObjects().each { spot ->
    def name = spot.getDisplayedName()?.toLowerCase()
    def roi = spot.getROI()
    if (roi == null) return

    def x = roi.getCentroidX()
    def y = roi.getCentroidY()

    if (name?.contains("channel 1")) {
        red << [x, y]
    } else if (name?.contains("channel 2")) {
        green << [x, y]
    }
}

println "🔴 Red detected: ${red.size()}, 🟢 Green detected: ${green.size()}"

// --- Count proximity pairs (yellow PLA) ---
int yellowCount = 0

red.each { r ->
    if (green.any { g ->
        def dx = r[0] - g[0]
        def dy = r[1] - g[1]
        Math.sqrt(dx*dx + dy*dy) <= maxDistPixels
    }) {
        yellowCount++
    }
}

println "🟡 Yellow PLA dots (red near green within ${maxDistMicrons} µm): ${yellowCount}"

// --- Add Yellow PLA count per cell to measurement table ---

// --- Add Yellow PLA count per cell to measurement table ---

def cells = getCellObjects()
def totalPLA = 0

cells.eachWithIndex { cell, i ->

    def children = cell.getChildObjects()

    def redSpots = children.findAll {
        it.getDisplayedName()?.toLowerCase()?.contains("channel 1")
    }

    def greenSpots = children.findAll {
        it.getDisplayedName()?.toLowerCase()?.contains("channel 2")
    }

    int yellowPerCell = 0

    redSpots.each { r ->
        def rX = r.getROI().getCentroidX()
        def rY = r.getROI().getCentroidY()

        def closeGreen = greenSpots.any { g ->
            def gX = g.getROI().getCentroidX()
            def gY = g.getROI().getCentroidY()
            def dx = rX - gX
            def dy = rY - gY
            Math.sqrt(dx * dx + dy * dy) <= maxDistPixels
        }

        if (closeGreen) yellowPerCell++
    }

    cell.getMeasurementList().putMeasurement("Yellow PLA", yellowPerCell)
    totalPLA += yellowPerCell

    println("Cell ${i + 1}: ${yellowPerCell} Yellow PLA")
}

println("✅ Total Yellow PLA dots across all cells: ${totalPLA}")


// --- Count subcellular spots ---
def redSpots = getDetectionObjects().findAll {
    it.getDisplayedName()?.toLowerCase()?.contains("channel 1")
}
def greenSpots = getDetectionObjects().findAll {
    it.getDisplayedName()?.toLowerCase()?.contains("channel 2")
}

def redCount = redSpots.size()
def greenCount = greenSpots.size()

// --- Count cells ---
def cellCount = getCellObjects().size()
println "🧫 Total number of cells (nuclei): ${cellCount}"



// --- Prepare export ---
def imageName = getProjectEntry()?.getImageName() ?: "UnknownImage"
def outputFile = new File("/Users/chia-chianhsieh/Desktop/total_pla_summary.csv")


// ✅ Append full row of data
outputFile << "${imageName},${yellowCount},${redCount},${greenCount},${cellCount}\n"

println "✅ Exported: ${imageName} | Yellow: ${yellowCount} | Red: ${redCount} | Green: ${greenCount} | Cells: ${cellCount}"
