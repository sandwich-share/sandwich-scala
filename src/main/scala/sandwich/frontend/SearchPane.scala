package sandwich.frontend

import scala.swing._
import java.awt.Dimension
import scala.swing.event.{ButtonClicked, EditDone}
import sandwich.client.filemanifesthandler.FileManifest
import sandwich.client.fileindex.FileItem
import scala.Array
import javax.swing.table.{TableCellEditor, AbstractTableModel, TableCellRenderer}
import javax.swing.{AbstractCellEditor, DefaultCellEditor, JButton, JTable}
import java.awt
import javax.swing.event.CellEditorListener
import java.util.EventObject
import java.awt.event.{ActionEvent, ActionListener}

/**
 * Sandwich
 * User: Brendan Higgins
 * Date: 9/24/13
 * Time: 4:09 AM
 */
class SearchPane(private val controller: FrontEndController) extends BoxPanel(Orientation.Vertical) {
  private val searchBox = new TextField
  searchBox.minimumSize = new Dimension(200, 30)
  searchBox.preferredSize = new Dimension(200, 30)
  searchBox.maximumSize = new Dimension(200, 30)
  private val scrollPane = new ScrollPane
  private val searchButton = new Button("Search: ")
  searchButton.action = Search

  contents += Swing.VStrut(20)
  contents += new BoxPanel(Orientation.Horizontal) {
    contents += Swing.HGlue
    contents += new BoxPanel(Orientation.Horizontal) {
      contents += searchButton
      contents += searchBox
      minimumSize = new Dimension(300, 30)
    }
    contents += Swing.HGlue
  }
  contents += Swing.VStrut(20)
  contents += scrollPane

  private object Search extends Action("Search") {
    override def apply() {
      val results = controller.search(searchBox.text).map(toTableItem(_)).toArray
      val table = new JTable(new AbstractTableModel {
        override def getRowCount: Int = results.length

        override def getColumnCount: Int = 3

        override def getValueAt(rowIndex: Int, columnIndex: Int): AnyRef = results(rowIndex)(columnIndex)

        override def isCellEditable(row: Int, column: Int): Boolean = if (column == 2) true else false
      })
      val columnModel = table.getColumnModel()
      columnModel.getColumn(1).setMaxWidth(200)
      columnModel.getColumn(1).setPreferredWidth(200)
      columnModel.getColumn(2).setMaxWidth(200)
      columnModel.getColumn(2).setPreferredWidth(200)
      columnModel.getColumn(2).setCellRenderer(new TableCellRenderer {
        def getTableCellRendererComponent(table: JTable, value: scala.Any, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): awt.Component = results(row)(column).asInstanceOf[JButton]
      })
      columnModel.getColumn(2).setCellEditor(new AbstractCellEditor() with TableCellEditor {
        override def getCellEditorValue: AnyRef = new AnyRef

        def getTableCellEditorComponent(table: JTable, value: scala.Any, isSelected: Boolean, row: Int, column: Int): awt.Component = results(row)(column).asInstanceOf[JButton]
      })
      scrollPane.contents = Component.wrap(table)
    }
  }

  private def toTableItem(fileItem: FileItem): Array[AnyRef] = {
    val downloadButton = new JButton("Download")
    downloadButton.addActionListener(new ActionListener {
      def actionPerformed(e: ActionEvent) {
        controller.download(fileItem)
      }
    })
    Array(fileItem.FileName, fileItem.Size.toString, downloadButton)
  }
}