Attribute VB_Name = "WeeklyTopProprietarySell"
Function GetWeeklyTopProprietarySellSheets() As Variant
    Dim ws As Worksheet
    Dim sheetNames(1 To 10) As String
    Dim sumValues(1 To 10) As Double
    Dim i As Integer

    ' Initialize sumValues to a very small number to ensure accurate comparisons
    For i = 1 To 10
        sheetNames(i) = ""
        sumValues(i) = -1 ' Use a negative value to represent uninitialized sums
    Next i

    ' Loop through each sheet in the workbook
    For Each ws In ThisWorkbook.Sheets
        If Not ShouldSkipSheet(ws.Name) Then
            Dim total As Double
    
            ' Calculate sum for range E3:E7
            total = WorksheetFunction.Sum(ws.Range("J3:J7"))
    
            ' Compare the sum value with the top 10
            For i = 1 To 10
                If total < sumValues(i) Or sumValues(i) = -1 Then
                    ' Shift existing values down
                    For j = 4 To i Step -1
                        sheetNames(j + 1) = sheetNames(j)
                        sumValues(j + 1) = sumValues(j)
                    Next j
                    ' Insert new values
                    sheetNames(i) = ws.Name
                    sumValues(i) = total
                    Exit For
                End If
            Next i
        End If
    Next ws

    ' Return the array containing top sheet names and sum values
    GetWeeklyTopProprietarySellSheets = sheetNames
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



