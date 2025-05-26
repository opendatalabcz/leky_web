import * as React from "react"
import { AdapterDateFns } from "@mui/x-date-pickers/AdapterDateFnsV3"
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider"
import { DatePicker } from "@mui/x-date-pickers/DatePicker"
import { cs } from "date-fns/locale"

type YearMonthPickerProps = {
    label: string
    value: Date | null
    onChange: (newValue: Date | null) => void
    minDate?: Date
    maxDate?: Date
}

export function YearMonthPicker({
                                    label,
                                    value,
                                    onChange,
                                    minDate,
                                    maxDate
                                }: YearMonthPickerProps) {
    return (
        <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={cs}>
            <DatePicker
                views={["year", "month"]}
                openTo="month"
                label={label}
                value={value}
                onChange={onChange}
                minDate={minDate}
                maxDate={maxDate}
                slotProps={{
                    textField: {
                        size: "small",
                        fullWidth: true,
                        sx: {
                            minWidth: { xs: "100%", sm: "160px" },
                            maxWidth: { sm: "240px" }
                        }
                    }
                }}
            />
        </LocalizationProvider>
    )
}