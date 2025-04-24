import ij.*
import ij.process.*
import ij.gui.*
import ij.plugin.*
import ij.WindowManager
import ij.plugin.CompositeConverter
 
// === PARAMETERS ===
def channelLUTs = [
	1: "Blue",    // Change channel 1 
    2: "Cyan",   // Change channel 2 
    3: "Red"       // Change channel 3 
]
 
// === SCRIPT START ===
def ids = WindowManager.getIDList()
if (ids == null || ids.length == 0) {
    IJ.showMessage("No images open!")
    return
}
 
for (id in ids) {
    def imp = WindowManager.getImage(id)
    if (imp == null || !(imp instanceof CompositeImage)) {
        IJ.log("Skipping non-composite image: " + (imp?.getTitle() ?: "Unknown"))
        continue
    }
 
    def ci = (CompositeImage) imp
    ci.setMode(CompositeImage.COMPOSITE)
 
    // Apply LUTs to specified channels
    channelLUTs.each { ch, lutName ->
        if (ch <= ci.getNChannels()) {
            ci.setC(ch)
            IJ.run(ci, lutName, "")
            IJ.log("Changed channel $ch to LUT: $lutName for image: ${ci.getTitle()}")
        } else {
            IJ.log("Image ${ci.getTitle()} has fewer than $ch channels, skipping channel $ch")
        }
    }
 
    ci.updateAndDraw()
}