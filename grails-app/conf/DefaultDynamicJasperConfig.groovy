import ar.com.fdvs.dj.domain.constants.Font
import ar.com.fdvs.dj.domain.constants.Border
import java.awt.Color
import ar.com.fdvs.dj.domain.constants.Transparency
import ar.com.fdvs.dj.domain.constants.HorizontalAlign
import ar.com.fdvs.dj.domain.constants.VerticalAlign
import ar.com.fdvs.dj.domain.constants.Rotation
import ar.com.fdvs.dj.domain.constants.Stretching
import ar.com.fdvs.dj.domain.constants.Page

dynamicJasper {

    useFullPageWidth = true

    page = Page.Page_A4_Landscape()

    isUsingImagesToAlign = true

    intPattern = '#0'
    floatPattern = '#0.00'
    datePattern = 'MM/dd/yyyy'

    titleStyle {
        font = new Font(18, Font._FONT_VERDANA, false)
        textColor = Color.black
        //border = Border.NO_BORDER
        borderColor = Color.darkGray
        horizontalAlign = HorizontalAlign.LEFT
        verticalAlign = VerticalAlign.MIDDLE
        backgroundColor = Color.white
        transparency = Transparency.OPAQUE
        transparent = false
        blankWhenNull = true
        padding = 2
        pattern = ''
        radius = 1
        rotation = Rotation.NONE
        stretching = Stretching.NO_STRETCH
        stretchWithOverflow = false
    }

    subtitleStyle {
        font = new Font(14, Font._FONT_VERDANA, false)
        textColor = Color.GREEN
        //border = Border.NO_BORDER
        borderColor = Color.darkGray
        horizontalAlign = HorizontalAlign.LEFT
        verticalAlign = VerticalAlign.MIDDLE
        backgroundColor = Color.white
        transparency = Transparency.OPAQUE
        transparent = false
        blankWhenNull = true
        padding = 2
        pattern = ''
        radius = 1
        rotation = Rotation.NONE
        stretching = Stretching.NO_STRETCH
        stretchWithOverflow = false
    }

    headerStyle {
        font = Font.ARIAL_MEDIUM_BOLD
        textColor = Color.white
        //border = Border.PEN_2_POINT
        borderColor = Color.darkGray
        horizontalAlign = HorizontalAlign.CENTER
        verticalAlign = VerticalAlign.MIDDLE
        backgroundColor = Color.blue
        transparency = Transparency.OPAQUE
        transparent = false
        blankWhenNull = true
        padding = 2
        pattern = ''
        radius = 1
        rotation = Rotation.NONE
        stretching = Stretching.NO_STRETCH
        stretchWithOverflow = false
    }

    detailStyle {
        font = Font.ARIAL_MEDIUM_BOLD
        textColor = Color.black
        //borderTop = Border.NO_BORDER
        //borderBottom = Border.THIN
        //borderLeft = Border.NO_BORDER
        //borderRight = Border.NO_BORDER
        borderColor = Color.blue
        horizontalAlign = HorizontalAlign.LEFT
        verticalAlign = VerticalAlign.MIDDLE
        backgroundColor = Color.white
        transparency = Transparency.OPAQUE
        //transparent = false
        blankWhenNull = true
        padding = 2
        //pattern = ''
        radius = 1
        rotation = Rotation.NONE
        stretching = Stretching.NO_STRETCH
        stretchWithOverflow = false
    }

}