import ij.IJ
import ij.WindowManager
import ij.ImagePlus

// Get all open images
def ids = WindowManager.getIDList()

if (ids == null) {
    IJ.showMessage("No images open!")
    return
}

for (id in ids) {
    def imp = WindowManager.getImage(id)
    if (imp == null) continue

    // Make the image the active one
    WindowManager.setTempCurrentImage(imp)

    // Run Max Intensity Z Projection
    IJ.run(imp, "Z Project...", "projection=[Max Intensity]")

    // Set composite mode to show all channels
    def projected = WindowManager.getCurrentImage()
    if (projected != null) {
        IJ.run(projected, "Make Composite", "")
        projected.setTitle(imp.getTitle() + "_MaxZ")
    }
}