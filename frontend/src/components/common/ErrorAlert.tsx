import { Alert, AlertTitle, Button, Box } from '@mui/material';
import { Refresh as RefreshIcon } from '@mui/icons-material';

interface ErrorAlertProps {
  title?: string;
  message: string;
  onRetry?: () => void;
}

export default function ErrorAlert({ title = 'Error', message, onRetry }: ErrorAlertProps) {
  return (
    <Box sx={{ py: 2 }}>
      <Alert
        severity="error"
        action={
          onRetry && (
            <Button
              color="inherit"
              size="small"
              onClick={onRetry}
              startIcon={<RefreshIcon />}
            >
              Retry
            </Button>
          )
        }
      >
        <AlertTitle>{title}</AlertTitle>
        {message}
      </Alert>
    </Box>
  );
}
