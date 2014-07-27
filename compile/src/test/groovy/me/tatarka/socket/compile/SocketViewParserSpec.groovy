package me.tatarka.socket.compile

import spock.lang.Shared
import spock.lang.Specification

import static SpecHelpers.xml

class SocketViewParserSpec extends Specification {
    @Shared
    def parser = new SocketViewParser()

    def "a single non-id view parses as an empty list"() {
        expect:
        parser.parse(xml { it.'TextView'() }) == []
    }

    def "a single view with an id parses as a single item"() {
        expect:
        parser.parse(xml {
            it.'TextView'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'android:id': '@+id/my_text_view'
            )
        }) == [View.of('android.widget.TextView', 'my_text_view').build()]
    }

    def "a non-id view with 2 children parses as two items"() {
        expect:
        parser.parse(xml {
            it.'LinearLayout'('xmlns:android': 'http://schemas.android.com/apk/res/android') {
                'TextView'('android:id': '@+id/my_text_view')
                'ImageView'('android:id': '@+id/my_image_view')
            }
        }) == [
                View.of('android.widget.TextView', 'my_text_view').build(),
                View.of('android.widget.ImageView', 'my_image_view').build()
        ]
    }

    def "an id view with non-id children parses as a single item"() {
        expect:
        parser.parse(xml {
            it.'LinearLayout'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'android:id': '@+id/my_linear_layout'
            ) {
                'TextView'()
                'ImageView'()
            }
        }) == [
                View.of('android.widget.LinearLayout', 'my_linear_layout').build()
        ]
    }

    def "an id view with id children parses as a single item with children"() {
        expect:
        parser.parse(xml {
            it.'LinearLayout'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'android:id': '@+id/my_linear_layout'
            ) {
                'TextView'('android:id': '@+id/my_text_view')
                'ImageView'('android:id': '@+id/my_image_view')
            }
        }) == [
                View.of('android.widget.LinearLayout', 'my_linear_layout')
                        .child(View.of('android.widget.TextView', 'my_text_view'))
                        .child(View.of('android.widget.ImageView', 'my_image_view'))
                        .build()
        ]
    }

    def "a view with an id but has a 'socket_ignore' attribute is not included"() {
        expect:
        parser.parse(xml {
            it.'TextView'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'xmlns:app': 'http://schemas.android.com/apk/res-auto',
                    'android:id': '@+id/my_text_view',
                    'app:socket_ignore': 'true'
            )
        }) == []
    }

    def "a view with a 'socket_field_name' attribute has a custom field name"() {
        expect:
        parser.parse(xml {
            it.'TextView'(
                    'xmlns:android': 'http://schemas.android.com/apk/res/android',
                    'xmlns:app': 'http://schemas.android.com/apk/res-auto',
                    'android:id': '@+id/my_text_view',
                    'app:socket_field_name': 'my_field_name'
            )
        }) == [View.of('android.widget.TextView', 'my_text_view').fieldName('my_field_name').build()]
    }
}