from kivy.app import App
from kivy.clock import Clock
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.button import Button
from kivy.uix.label import Label
from kivy.uix.popup import Popup
from kivy.uix.textinput import TextInput


class DigitLabel(Label):
    def __init__(self, **kwargs):
        super(DigitLabel, self).__init__(**kwargs)
        self.font_size = '48sp'
        self.text = "0"
        self.index = 0  # Current index in the number cycle
        self.cycle = [str(i) for i in range(10)]  # The cycle of numbers to spin through
        self.event = None  # Clock event for spinning

    def start_spinning(self):
        self.event = Clock.schedule_interval(self.update_digit, 0.1)

    def stop_spinning(self, final_digit, delay):
        def stop(dt):
            if self.event:
                Clock.unschedule(self.event)
                self.text = final_digit
        Clock.schedule_once(stop, delay)

    def update_digit(self, dt):
        self.index = (self.index + 1) % len(self.cycle)
        self.text = self.cycle[self.index]

class SlotMachineApp(App):
    def build(self):
        self.win_number = "0008"  # Winning number, for example purposes
        self.user_guess = None

        self.start_layout = BoxLayout(orientation='vertical', size_hint=(None, None), size=(200, 50),
                                      pos_hint={'center_x': .5, 'center_y': .5})
        start_button = Button(text='Start Playing', font_size='20sp')
        start_button.bind(on_press=self.setup_game)
        self.start_layout.add_widget(start_button)
        return self.start_layout

    def setup_game(self, instance):
        self.root.clear_widgets()
        self.game_layout = BoxLayout(orientation='vertical', padding=(10, 50))

        digits_layout = BoxLayout(size_hint=(1, None), spacing=50, height=100)
        self.digits = [DigitLabel() for _ in range(4)]
        for digit_label in self.digits:
            digits_layout.add_widget(digit_label)
        self.game_layout.add_widget(digits_layout)

        self.spin_button = Button(text='Spin', font_size='20sp', size_hint=(None, None), size=(200, 50),
                                  pos_hint={'center_x': .5, 'center_y': .5})
        self.spin_button.bind(on_press=self.ask_user_guess)
        self.game_layout.add_widget(self.spin_button)

        self.root.add_widget(self.game_layout)

    def ask_user_guess(self, instance):
        if not getattr(self, 'is_counting_down', False):
            content = BoxLayout(orientation='vertical')
            text_input = TextInput(text='', multiline=False)
            submit_button = Button(text='Submit', size_hint=(1, 0.2))
            content.add_widget(text_input)
            content.add_widget(submit_button)

            self.popup = Popup(title="Enter your guess (4 digits)",
                               content=content,
                               size_hint=(None, None), size=(400, 200))
            submit_button.bind(on_press=lambda x: self.validate_guess(text_input.text))
            self.popup.open()

    def validate_guess(self, guess):
        if len(guess) == 4 and guess.isdigit():
            self.user_guess = guess
            self.popup.dismiss()
            self.spin_button_clicked()
        else:
            invalid_input_popup = Popup(title="Invalid Input",
                                        content=Label(text="Please enter a 4-digit number."),
                                        size_hint=(None, None), size=(400, 200))
            invalid_input_popup.open()

    def spin_button_clicked(self, *args):
        self.spin_button.disabled = True
        self.is_counting_down = True
        for digit_label in self.digits:
            digit_label.start_spinning()
        # Stop each digit sequentially
        for i, digit_label in enumerate(self.digits):
            digit_label.stop_spinning(self.win_number[i], delay=1+i)

        # Schedule display_result after all digits have stopped
        Clock.schedule_once(lambda dt: self.display_result(), 1+len(self.digits))

    def display_result(self):
        self.spin_button.disabled = False
        if self.user_guess == self.win_number:
            message = f'Congratulations! You guessed {self.user_guess} correctly. You\'re a winner!'
        else:
            message = f'Sorry, you guessed {self.user_guess}. The correct number was {self.win_number}. Better luck next time!'
        message_label = Label(text=message, valign='middle', halign='center')
        message_label.size = (400, 200)
        message_label.text_size = message_label.size
        message_label.bind(size=message_label.setter('text_size'))
        self.result_popup = Popup(title='Result', content=message_label, size_hint=(None, None), size=(400, 200))
        self.result_popup.open()

if __name__ == '__main__':
    SlotMachineApp().run()
