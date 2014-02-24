class DynamicJasperGrailsPlugin {

    def version = 0.6
    def loadAfter = ['domainClass']

    def author = 'Alejandro Gomez (FDV Solutions)'
    def authorEmail = 'alejandro.gomez@fdvsolutions.com'
    def title = 'Integrates DynamicJasper into Grails'
    def description = '''\
Automates reports generation for your domail model.
'''

    def documentation = 'http://grails.org/DynamicJasper+Plugin'

    def doWithSpring = {
        // do nothing
    }

    def doWithApplicationContext = { applicationContext ->
        // do nothing
    }

    def doWithWebDescriptor = { xml ->
        // do nothing
    }

    def doWithDynamicMethods = { ctx ->
        // do nothing
    }

    def onChange = { event ->
        // do nothing
    }

    def onConfigChange = { event ->
        // do nothing
    }
}
