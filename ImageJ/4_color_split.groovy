import ij.IJ
import ij.WindowManager
import ij.ImagePlus
import ij.io.FileSaver
import ij.process.ImageProcessor
import java.io.File

// Prompt user to choose a folder to save images
def dir = IJ.getDirectory("Choose a folder to save images")
if (dir == null) {
    IJ.showMessage("No folder selected. Script cancelled.")
    return
}

// Create a directory to save the channel images
def saveDir = dir + File.separator + "4Color_Channels"
new File(saveDir).mkdirs()

// Get all open image IDs
def ids = WindowManager.getIDList()
if (ids == null || ids.length == 0) {
    IJ.showMessage("No images open!")
    return
}

for (id in ids) {
    def imp = WindowManager.getImage(id)
    if (imp == null || imp.getNChannels() != 4) continue // Check for 4-channel images

    // Split the image into its individual channels (assuming 4 channels)
    for (int channel = 1; channel <= 4; channel++) {
        imp.setC(channel)  // Set to the current channel
        def ip = imp.getProcessor()

        // Create a new ImagePlus for this channel
        def channelImp = new ImagePlus(imp.getTitle() + "_Channel_" + channel, ip)

        // Save the channel as a .tif file
        def baseTitle = imp.getTitle().replace(".tif", "")
        new FileSaver(channelImp).saveAsTiff(saveDir + File.separator + baseTitle + "_Channel_" + channel + ".tif")

        // Clean up
        channelImp.close()
    }
}

IJ.showMessage("Done!", "4-color channels have been saved in the '4Color_Channels' folder.")