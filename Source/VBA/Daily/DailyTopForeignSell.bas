Attribute VB_Name = "DailyTopForeignSell"
Function GetTopForeignSellSheets() As Variant
    Dim ws As Worksheet
    Dim sheetNames(1 To 10) As String
    Dim values(1 To 10) As Double
    Dim i As Integer

    For i = 1 To 10
        sheetNames(i) = ""
        values(i) = 0
    Next i

    For Each ws In ThisWorkbook.Sheets
        If Not ShouldSkipSheet(ws.Name) Then
            If IsNumeric(ws.Range("E3").Value) Then
                For i = 1 To 10
                    If ws.Range("E3").Value < values(i) Then
                        ' Shift existing values down
                        For j = 4 To i Step -1
                            sheetNames(j + 1) = sheetNames(j)
                            values(j + 1) = values(j)
                        Next j
                        ' Insert new values
                        sheetNames(i) = ws.Name
                        values(i) = ws.Range("E3").Value
                        Exit For
                    End If
                Next i
            End If
        End If
    Next ws

    GetTopForeignSellSheets = sheetNames
End Function

Function ShouldSkipSheet(sheetName As String) As Boolean
    ' List of sheets to skip
    Dim sheetsToSkip As Variant
    sheetsToSkip = Array("MoneyFlow", "Top", "RS", "GDNN", "VN30", "VNINDEX", "Shortcut")

    ' Check if the sheet name is in the list
    ShouldSkipSheet = IsInArray(sheetName, sheetsToSkip)
End Function

Function IsInArray(valToBeFound As Variant, arr As Variant) As Boolean
    Dim element As Variant
    For Each element In arr
        If element = valToBeFound Then
            IsInArray = True
            Exit Function
        End If
    Next element
    IsInArray = False
End Function

