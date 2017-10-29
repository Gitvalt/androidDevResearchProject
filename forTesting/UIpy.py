import kivy
import mainApp
from kivy.app import App
from kivy.core.window import Window
from kivy.lang import Builder
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.gridlayout import GridLayout
from kivy.uix.label import Label
from kivy.uix.recycleview import RecycleView
from kivy.uix.screenmanager import Screen, ScreenManager, SlideTransition
from kivy.uix.textinput import TextInput

#check kivy version
kivy.require('1.10.0')

#Recycler element. ! widget by type x must be type x
Builder.load_string('''
<Recycler>:
    viewclass: 'Button'
    RecycleBoxLayout:
        border: [10,10,10,10]
        default_size: None, dp(56)
        default_size_hint: 1, None
        size_hint_y: None
        height: self.minimum_height
        orientation: 'vertical'
''')

class Recycler(RecycleView):
    def __init__(self, **kwargs):
        super(Recycler, self).__init__(**kwargs)
        
        devices = mainApp.findDevices()
        if devices is None:
            print "No devices found"
            self.data = [{'text': 'None'}]
        else: 
            self.data = [{'text': str(x)} for x in devices]


class mainWindow(BoxLayout):
    def test():
        pass



class mainWindowApp(App):

    welcomeMsg = "Hello user!"

    def build(self):
        #return Recycler()
        return mainWindow()

if __name__ == '__main__':
    mainWindowApp().run()
