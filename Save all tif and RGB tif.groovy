import ij.IJ
import ij.WindowManager
import ij.io.FileSaver
import ij.ImagePlus
import ij.process.ImageConverter
import java.io.File

// Prompt user to choose a folder to save images
def dir = IJ.getDirectory("Choose a folder to save images")
if (dir == null) {
    IJ.showMessage("No folder selected. Script cancelled.")
    return
}

// Get all open image IDs
def ids = WindowManager.getIDList()
if (ids == null || ids.length == 0) {
    IJ.showMessage("No images open!")
    return
}

for (id in ids) {
    def imp = WindowManager.getImage(id)
    if (imp == null) continue

    def title = imp.getTitle().replace(".tif", "") // Remove .tif extension
    def basePath = dir + File.separator + title

    // Save original as .tif
    def fs = new FileSaver(imp)
    fs.saveAsTiff(basePath + ".tif")

    // Duplicate and convert to RGB
    def rgbImp = imp.duplicate()
    new ImageConverter(rgbImp).convertToRGB()
    def fsRgb = new FileSaver(rgbImp)
    fsRgb.saveAsTiff(basePath + "_RGB.tif")
    
    rgbImp.close() // Clean up
}

IJ.showMessage("Done!", "All images saved as TIF and RGB TIF.")