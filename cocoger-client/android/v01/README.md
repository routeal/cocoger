## Service

## UI

### Activity

* Top level UI
* Should be a wrapper of Fragment
* Should be a pair of XXXActivity and XXXFragment
* Resource: activity_xxx.xml
* Resource: fragment_xxx.xml

### InfoFragment

* InfoWindow's fragment
* Resource: fragment\_xxx_info.xml

### DialogFragment

* Dialog fragment
* Resource: fragment\_xxx_dialog.xml

### ListFragment

* Extends PagerFragment
* ListViewHolder
* Resource: fragment\_xxx_list.xml
* Resource: listview\_xxx_item.xml

### Manager

* better be a singleton
* common api: add/change/remove
* common listener
* common event flow