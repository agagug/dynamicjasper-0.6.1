import ar.com.fdvs.dj.core.DynamicJasperHelper
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager
import ar.com.fdvs.dj.domain.*
import ar.com.fdvs.dj.domain.constants.HorizontalAlign
import ar.com.fdvs.dj.domain.entities.DJGroup
import ar.com.fdvs.dj.domain.entities.DJGroupVariable
import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn
import ar.com.fdvs.dj.domain.entities.columns.ExpressionColumn
import ar.com.fdvs.dj.domain.entities.columns.SimpleColumn
import ar.com.fdvs.dj.domain.entities.conditionalStyle.ConditionalStyle
import ar.com.fdvs.dj.domain.entities.conditionalStyle.StatusLightCondition
import ar.com.fdvs.dj.output.ReportWriter
import ar.com.fdvs.dj.output.ReportWriterFactory
import grails.util.GrailsUtil
import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter
import org.apache.commons.beanutils.BeanUtils
import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler
import org.codehaus.groovy.grails.commons.GrailsClass
import org.codehaus.groovy.grails.plugins.PluginManagerHolder

import java.awt.*
import java.lang.reflect.InvocationTargetException

class DjReportController {

    def image = {
        def pluginPath = PluginManagerHolder.pluginManager.getGrailsPlugin('dynamic-jasper').getPluginPath()
        redirect(uri: "${pluginPath}/images/${params.image}.gif")
    }

    def index = {
        if (params.report) {
            namedReport()
        } else if (params.entity) {
            entityReport()
        } else {
            handleError()
        }
    }

    def handleError() {
        redirect(uri: '/')
    }

    def namedReport() {
        def config = loadConfig()
        def reportConfig = config[params.report]
        if (reportConfig) {
            config.merge(reportConfig)
            GrailsClass domainClass = grailsApplication.getArtefactByLogicalPropertyName(DomainClassArtefactHandler.TYPE, config.entity)
            doReport(domainClass, config, params, request, response)
        } else {
            handleError()
        }
    }

    def entityReport() {
        GrailsClass domainClass = grailsApplication.getArtefactByLogicalPropertyName(DomainClassArtefactHandler.TYPE, params.entity)
        assert domainClass
        def reportable = getPropertyValue(domainClass.clazz, 'reportable')
        //because an empty map also means this class is reportable
        if (reportable != null) {
            def config = loadAndMergeConfig(domainClass, reportable, params)
            doReport(domainClass, config, params, request, response)
        } else {
            handleError()
        }
    }

    def doReport(def domainClass, def config, def params, def request, def response) {
        DynamicReport report = new DynamicReport()
        report.options = new DynamicReportOptions()
        report.options.useFullPageWidth = config.useFullPageWidth
        report.options.page = config.page
        report.title = config.title ?: "${domainClass?.naturalName} Report"
        setPropertyIfNotNull(report, 'titleStyle', getStyle(config.titleStyle))
        setPropertyIfNotNull(report, 'subtitleStyle', getStyle(config.subtitleStyle))
        setPropertyIfNotNull(report.options, 'titleHeight', config.titleHeight)
        setPropertyIfNotNull(report, 'subtitle', config.subtitle)
        setPropertyIfNotNull(report, 'autoTexts', config.autoTexts)
        setPropertyIfNotNull(report.options, 'subtitleHeight', config.subtitleHeight)
        setPropertyIfNotNull(report.options, 'detailHeight', config.detailHeight)
        setPropertyIfNotNull(report.options, 'useFullPageWidth', config.useFullPageWidth)
        report.options.defaultDetailStyle = getStyle(config.detailStyle)
        report.options.defaultHeaderStyle = getStyle(config.headerStyle)

        def columnNames = config.columns ?: getPropertyValue(domainClass.clazz, 'reportColumns') ?: domainClass.properties.name - ['id', 'version']

        def columns = addColumns(config, domainClass, report, columnNames)

        def groupColumns = config.groupColumns
        if (groupColumns) {
            groupColumns.each {groupColumn ->
                DJGroup group = new DJGroup()
                group.columnToGroupBy = columns[(groupColumn)]
                config.groupFooterColumns.eachWithIndex { groupFooterColumn, index ->
                    group.footerVariables << new DJGroupVariable(columns[(groupFooterColumn)], getPropertyValue(DJCalculation, config.groupOperations[index]))
                }
                report.columnsGroups << group
            }
        }

        def items
        if (config.dataSource) {
            items = config.dataSource.call(session, params)
        } else if (groupColumns) {
            items = domainClass.clazz.findAll("from $domainClass.clazz.name as s order by ${groupColumns.join(',')}")
        } else {
            items = domainClass.clazz.list()
        }

        JRDataSource dataSource = new JRBeanCollectionDataSource(items)
        JasperPrint print = DynamicJasperHelper.generateJasperPrint(report, new ClassicLayoutManager(), dataSource)
        def reportFileName = config.fileName
        def reportFormat = params.reportFormat ?: 'PDF'
        ReportWriter reportWriter = ReportWriterFactory.getInstance().getReportWriter(print, reportFormat, [(JRHtmlExporterParameter.IMAGES_URI): "${request.contextPath}/djReport/image?image=".toString(), (JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN): config.isUsingImagesToAlign])
        if (reportFileName) {
            response.addHeader('content-disposition', "attachment; filename=${reportFileName}.${reportFormat.toLowerCase()}")
        }
        reportWriter.writeTo(response)
    }


    private ArrayList createConditionalStyles(Style baseStyle) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
     		Style style0 = (Style) BeanUtils.cloneBean(baseStyle);
     		style0.setTextColor(Color.RED);
     		style0.setFont(Font.ARIAL_MEDIUM);
     		Style style1 = (Style) BeanUtils.cloneBean(baseStyle);
     		style1.setTextColor(Color.blue);
     		Style style2 = (Style) BeanUtils.cloneBean(baseStyle);
     		style2.setTextColor(new Color(0,128,0)); //dark green
     		style2.setFont(Font.COMIC_SANS_BIG_BOLD);

     		StatusLightCondition status0 = new StatusLightCondition(new Double(1), new Double(20)); //TODO ENHANCEMENT make it come from a parameter??? $P{...}
     		StatusLightCondition status1 = new StatusLightCondition(new Double(20), new Double(1000));
            // StatusLightCondition status2 = new StatusLightCondition(new Double(20),new Double(100000));

     		ConditionalStyle condition0 = new ConditionalStyle(status0,style0);
     		ConditionalStyle condition1 = new ConditionalStyle(status1,style1);
     		// ConditionalStyle condition2 = new ConditionalStyle(status2,style2);

     		ArrayList conditionalStyles = new ArrayList();
     		conditionalStyles.add(condition0);
     		conditionalStyles.add(condition1);
     		//conditionalStyles.add(condition2);
     		return conditionalStyles;
     	}



    def loadAndMergeConfig(def domainClass, def classConfig, def params) {
        def config = loadConfig()
        Properties props = new Properties()
        setPropertyIfNotNull(props, 'entity', params.entity)
        setPropertyIfNotNull(props, 'title', "${classConfig.title} ${params.lotteryName}")
        setPropertyIfNotNull(props, 'columns', params.reportColumns?.split(',') ?: classConfig.columns ?: domainClass.properties.name - ['id', 'version'])
        setPropertyIfNotNull(props, 'patterns', classConfig.patterns)
        setPropertyIfNotNull(props, 'columnTitles', classConfig.columnTitles)
        setPropertyIfNotNull(props, 'groupColumns', params.groupColumns?.split(',') ?: classConfig.groupColumns)
        setPropertyIfNotNull(props, 'groupFooterColumns', params.groupFooterColumns?.split(',') ?: classConfig.groupFooterColumns)
        setPropertyIfNotNull(props, 'groupOperations', params.groupOperations?.split(',') ?: classConfig.groupOperations ?: ('SUM,' * (props.groupFooterColumns?.size() ?: 0)).split(','))
        setPropertyIfNotNull(props, 'dataSource', classConfig.dataSource)
        setPropertyIfNotNull(props, 'fileName', classConfig.fileName)
        setPropertyIfNotNull(props, 'useFullPageWidth', classConfig.useFullPageWidth)
        setPropertyIfNotNull(props, 'page', classConfig.page)
        setPropertyIfNotNull(props, 'intPattern', classConfig.intPattern)
        setPropertyIfNotNull(props, 'floatPattern', classConfig.floatPattern)
        setPropertyIfNotNull(props, 'datePattern', classConfig.datePattern)
        setPropertyIfNotNull(props, 'titleStyle', classConfig.titleStyle)
        setPropertyIfNotNull(props, 'subtitleStyle', classConfig.subtitleStyle)
        setPropertyIfNotNull(props, 'headerStyle', classConfig.headerStyle)
        setPropertyIfNotNull(props, 'detailStyle', classConfig.detailStyle)
        setPropertyIfNotNull(props, 'autoTexts', classConfig.autoTexts)
        setPropertyIfNotNull(props, 'isUsingImagesToAlign', classConfig.isUsingImagesToAlign)
        config.merge(new ConfigSlurper(GrailsUtil.environment).parse(props))
        return config
    }

    def addColumns(def config, def domainClass, def report, def columnNames) {
        AbstractColumn columnState
        ArrayList conditionalStyles

        def columns = [:]
        columnNames.each { propertyName ->
            def property = getProperty(domainClass, propertyName)
            def column = new SimpleColumn()
            def propertyType
            switch (property.type) {
                case int:
                    propertyType = Integer
                    break
                case char:
                    propertyType = Character
                    break
                case byte:
                case short:
                case long:
                case float:
                case double:
                case boolean:
                    propertyType = Class.forName('java.lang.' + StringUtils.capitalize(property.type.name))
                    break
                case Number:
                case Boolean:
                case Character:
                case Date:
                case String:
                    propertyType = property.type
                    break
                default:
                    report.fields << new ColumnProperty(propertyName, property.type.name)
                    column = new ExpressionColumn()
                    column.expression = new ToStringCustomExpression(propertyName)
                    propertyType = String
            }
            column.columnProperty = new ColumnProperty(propertyName, propertyType.name)
            column.title = config.columnTitles?."${propertyName}" ?: property.naturalName
            def style = getStyle(config.detailStyle)
            if (Number.isAssignableFrom(propertyType) || Date.isAssignableFrom(propertyType)) {
                style.horizontalAlign = HorizontalAlign.RIGHT
            } else {
                style = getStyle(config.detailStyle)
            }
            def propertyPattern
            if (config.patterns?."${propertyName}") {
                propertyPattern = config.patterns?."${propertyName}"
            } else {
                switch (propertyType) {
                    case Byte:
                    case Short:
                    case Integer:
                    case Long:
                        propertyPattern = config.intPattern
                        break;
                    case Float:
                    case Double:
                        propertyPattern = config.floatPattern
                        break;
                    case Date:
                        propertyPattern = config.datePattern
                        break;
                    default:
                        propertyPattern = null
                }
            }
            style.pattern = propertyPattern
            column.style = style


            /*
            if (propertyName == 'status') {
                Style amountStyle = new Style();
                amountStyle.setFont(new Font(12, Font._FONT_TIMES_NEW_ROMAN, false))
                amountStyle.setTextColor(Color.black);

                // conditionalStyles = createConditionalStyles(amountStyle);

                 columnState = ColumnBuilder.getNew().setColumnProperty("status", Class.forName(property.type.name))
                  			.setTitle("status").setWidth(new Integer(70))
                            // .addConditionalStyles(conditionalStyles)
                  			.setStyle(amountStyle).build();



                report.columns << columnState
                columns[(propertyName)] = columnState
            }
            else
            {
                */
                report.columns << column
                columns[(propertyName)] = column
            //}

            //report.columns << column
            //columns[(propertyName)] = column
        }
        return columns
    }

    def getProperty(def domainClass, def propertyName) {
        def property
        propertyName.tokenize('.').each { part ->
            property = domainClass.properties.find { prop ->
                prop.name == part
            }
            def name = property.type.simpleName
            domainClass = grailsApplication.getArtefactByLogicalPropertyName(DomainClassArtefactHandler.TYPE, name[0].toLowerCase() + name[1 .. - 1])
        }
        return property
    }

    def getPropertyValue(def clazz, def propertyName) {
        clazz.metaClass.hasProperty(clazz, propertyName)?.getProperty(clazz)
    }

    def setPropertyIfNotNull(def target, def propertyName, def value) {
        if (value != null && (!(value instanceof ConfigObject) || !(value.isEmpty()))) {
            target[propertyName] = value
        }
    }

    private ConfigObject loadConfig() {
        def config = ConfigurationHolder.config
        GroovyClassLoader classLoader = new GroovyClassLoader(getClass().classLoader)
        config.merge(new ConfigSlurper(GrailsUtil.environment).parse(classLoader.loadClass('DefaultDynamicJasperConfig')))
        try {
            config.merge(new ConfigSlurper(GrailsUtil.environment).parse(classLoader.loadClass('DynamicJasperConfig')))
        } catch (Exception ignored) {
            // ignore, just use the defaults
        }
        return new ConfigSlurper(GrailsUtil.environment).parse(new Properties()).merge(config.dynamicJasper)
    }

    def getStyle(def styleConfig) {
        def style = new Style()
        style.font = styleConfig.font
        /*
        if (styleConfig.border) {
            style.border = styleConfig.border
        } else {
            style.borderTop = styleConfig.borderTop
            style.borderBottom = styleConfig.borderBottom
            style.borderLeft = styleConfig.borderLeft
            style.borderRight = styleConfig.borderRight
        }
        */
        style.backgroundColor = styleConfig.backgroundColor
        style.transparency = styleConfig.transparency
        //style.transparent = styleConfig.transparent
        style.textColor = styleConfig.textColor
        style.horizontalAlign = styleConfig.horizontalAlign
        style.verticalAlign = styleConfig.verticalAlign
        style.blankWhenNull = styleConfig.blankWhenNull

        //style.borderColor = styleConfig.borderColor
        /*
        if (style.padding) {
            style.padding = styleConfig.padding
        } else {
            style.paddingTop = styleConfig.paddingTop
            style.paddingBotton = styleConfig.paddingBotton
            style.paddingLeft = styleConfig.paddingLeft
            style.paddingRight = styleConfig.paddingRight
        }
        */
        //style.pattern = styleConfig.pattern
        style.radius = styleConfig.radius
        style.rotation = styleConfig.rotation
        //FIXME typo in DJ API
        //style.streching = styleConfig.stretching
        //style.stretchWithOverflow = styleConfig.stretchWithOverflow
        style
    }
}
