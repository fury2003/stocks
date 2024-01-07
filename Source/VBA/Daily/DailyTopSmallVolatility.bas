Attribute VB_Name = "DailyTopSmallVolatility"
Function GetTopSmallVolatilitySheets() As Variant
    Dim ws As Worksheet
    Dim sheetNames(1 To 5) As String
    Dim i As Integer
    Dim j As Integer

    ' Initialize arrays
    For i = 1 To 5
        sheetNames(i) = ""
    Next i

    ' Loop through sheets and find top 5 with values less than 1%
    For Each ws In ThisWorkbook.Sheets
        If Not ShouldSkipSheet(ws.Name) Then
            Dim percentageValue As Double
            If Not IsEmpty(ws.Range("V3").Value) And IsNumeric(ws.Range("V3").Value) Then
                ' Convert percentage to numeric value
                percentageValue = CDbl(Replace(ws.Range("V3").Value, "%", ""))
    
                If percentageValue < 0.01 Then ' Check if less than 1%
                    For i = 1 To 5
                        ' Shift existing values down
                        For j = 4 To i Step -1
                            sheetNames(j + 1) = sheetNames(j)
                        Next j
                        ' Insert new values
                        sheetNames(i) = ws.Name
                        Exit For
                    Next i
                End If
            End If
        End If
    Next ws

    GetTopSmallVolatilitySheets = sheetNames
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
