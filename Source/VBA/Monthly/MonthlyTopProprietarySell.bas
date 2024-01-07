Attribute VB_Name = "MonthlyTopProprietarySell"
Function GetMonthlyTopProprietarySellSheets() As Variant
    Dim ws As Worksheet
    Dim sheetNames(1 To 5) As String
    Dim sumValues(1 To 5) As Double
    Dim i As Integer

    ' Initialize sumValues to a very small number to ensure accurate comparisons
    For i = 1 To 5
        sheetNames(i) = ""
        sumValues(i) = -1 ' Use a negative value to represent uninitialized sums
    Next i

    ' Loop through each sheet in the workbook
    For Each ws In ThisWorkbook.Sheets
        Dim total As Double

        ' Calculate sum for range E3:E7
        total = WorksheetFunction.Sum(ws.Range("J3:J25"))

        ' Compare the sum value with the top 5
        For i = 1 To 5
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
    Next ws

    ' Return the array containing top sheet names and sum values
    GetMonthlyTopProprietarySellSheets = sheetNames
End Function




