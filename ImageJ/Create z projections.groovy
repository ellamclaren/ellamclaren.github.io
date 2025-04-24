import ij.IJ
import ij.WindowManager
import ij.ImagePlus

// Get all open image IDs
def ids = WindowManager.getIDList()
if (ids == null) {
    IJ.showMessage("No images open!")
    return
}

for (id in ids) {
    def imp = WindowManager.getImage(id)
    if (imp == null) continue

    // Set as active
    WindowManager.setTempCurrentImage(imp)

    // Run Max Intensity Z-Projection
    IJ.run(imp, "Z Project...", "projection=[Max Intensity]")

    // Get the projected image
    def proj = WindowManager.getCurrentImage()
    if (proj == null) continue

    // Convert to Composite so all channels are visible
    IJ.run(proj, "Make Composite", "")

    // Optional: rename projection for clarity
    proj.setTitle(imp.getTitle() + "_MaxZ")

    // Close the original
    imp.close()
}

IJ.showMessage("Done!", "Max projections complete. Originals closed.")